package ee.ria.dhx.server.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Document;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.entity.Recipient;
import ee.ria.dhx.server.entity.Sender;
import ee.ria.dhx.server.entity.Transport;
import ee.ria.dhx.server.repository.ClassificatorRepository;
import ee.ria.dhx.server.repository.DocumentRepository;
import ee.ria.dhx.server.repository.FolderRepository;
import ee.ria.dhx.server.repository.OrganisationRepository;
import ee.ria.dhx.server.repository.RecipientRepository;
import ee.ria.dhx.server.service.util.AttachmentUtil;
import ee.ria.dhx.server.service.util.StatusEnum;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
@Transactional
public class DhxDocumentService {

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  CapsuleConfig capsuleConfig;

  @Autowired
  FolderRepository folderRepository;

  @Autowired
  OrganisationRepository organisationRepository;

  @Autowired
  AddressService addressService;

  @Autowired
  ClassificatorRepository classificatorRepository;

  @Autowired
  DocumentRepository documentRepository;

  @Autowired
  RecipientRepository recipientRepository;

  @Autowired
  AsyncDhxPackageService asyncDhxPackageService;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  @Value("${dhx.server.treat-cantainer-as-string}")
  Boolean treatContainerAsString;

  public SendDocumentsResponse receiveDocuments(SendDocuments documents,
      InternalXroadMember sender, InternalXroadMember recipient) throws DhxException {
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    try {
      if (documents.getKeha().getDokumendid() == null
          || documents.getKeha().getDokumendid().getHref() == null) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
      }
      schemaStream = FileUtil.getFileAsStream(capsuleConfig
          .getXsdForVersion(capsuleConfig
              .getCurrentCapsuleVersion()));
      capsuleStream =
          AttachmentUtil.base64decodeAndUnzip(documents.getKeha().getDokumendid().getHref()
              .getInputStream());
      DecContainer container = null;
      String containerString = null;
      // TODO: think of the alternative to reading into string
      if (treatContainerAsString) {
        containerString = AttachmentUtil.readInput(capsuleStream);
        stringStream = new ByteArrayInputStream(containerString.getBytes("UTF-8"));
        container =
            (DecContainer) dhxMarshallerService.unmarshallAndValidate(stringStream, schemaStream);
      } else {
        container =
            (DecContainer) dhxMarshallerService
                .unmarshallAndValidate(capsuleStream, schemaStream);
      }
      Document document =
          getDocumentFromContainer(container, sender, recipient, documents.getKeha().getKaust(),
              containerString);
      documentRepository.save(document);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting or unpacking attachment");
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(schemaStream);
      FileUtil.safeCloseStream(stringStream);
    }

    return null;
  }

  private Document getDocumentFromContainer(DecContainer container,
      InternalXroadMember senderMember, InternalXroadMember recipientMember, String folderName,
      String containerString) throws DhxException {
    Document document = new Document();
    Folder folder = folderRepository.findByName(folderName);
    document.setFolder(folder);
    DhxOrganisation dhxSenderOrg = new DhxOrganisation(senderMember);
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
            dhxSenderOrg.getSystem());
    if (senderOrg == null) {
      throw new DhxException(DhxExceptionEnum.WRONG_SENDER, "Unable to find senders organisation");
    }
    document.setOrganisation(senderOrg);
    document.setContent(containerString);
    Transport transport = new Transport();
    document.addTransport(transport);
    Sender sender = new Sender();
    transport.addSender(sender);
    sender.setOrganisation(senderOrg);
    sender.setTransport(transport);
    Classificator classificator =
        classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
    for (DecRecipient containerRecipient : container.getTransport().getDecRecipient()) {
      Recipient recipient = new Recipient();
      recipient.setTransport(transport);
      recipient.setStruCturalUnit(containerRecipient.getStructuralUnit());
      recipient.setStatus(classificator);
      // recipient.setRecipientStatus(recipientStatus);
      recipient.setPersonalcode(containerRecipient.getPersonalIdCode());
      DhxOrganisation dhxRecipientOrg = new DhxOrganisation(recipientMember);
      Organisation recipientOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
              dhxRecipientOrg.getSystem());
      if (recipientOrg == null) {
        throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
            "Unable to find senders organisation");
      }
      recipient.setOrganisation(findOrg(containerRecipient.getOrganisationCode()));
      transport.addRecipient(recipient);
    }
    // List<tran>
    // document.setTransports(transports);
    return document;
    // transport.setStatus();
  }

  private Organisation findOrg(String containerOrganisationId) throws DhxException {
    Organisation org = null;
    log.debug("Searching member by organisationId:" + containerOrganisationId);
    InternalXroadMember member = null;
    try {
      member = addressService.getClientForMemberCode(containerOrganisationId, null);
    } catch (DhxException ex) {
      log.debug(
          "Erro occured while searching org. ignoring error and continue!" + ex.getMessage(), ex);
    }
    // if member not found, then try to find by registration code and subsystem, by splitting
    // adressee from container
    if (member == null) {
      Integer index = containerOrganisationId.lastIndexOf(".");
      if (index > 0) {
        String code = containerOrganisationId.substring(index);
        String system = containerOrganisationId.substring(0, index);
        log.debug("Searching member by code:" + code + " and subsystem: " + system);
        member = addressService.getClientForMemberCode(code, system);
      }
      
      
      if (member == null) {
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Unable to find member in addressregistry by regsitration code: "
                + containerOrganisationId);
      }

    } 
    org = organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
      member.getSubsystemCode());
    if (org == null) {
    throw new DhxException(DhxExceptionEnum.DATA_ERROR,
        "Unable to find organisation using member: "
            + member.toString());
    }
    return org;
    // find by organisationId
    // split and find by organisation id and subsystem
    // split and find by org id and DHX or DHX. + subsystem
  }



  public void sendDocumentsToDhx() {
    Classificator status =
        classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
    List<Recipient> recipients = recipientRepository.findByStatusAndDhxInternalConsignmentIdNull(status);
    for (Recipient recipient : recipients) {
      InputStream containerStream = null;
      try {
        containerStream =
            new ByteArrayInputStream(recipient.getTransport().getDokument().getContent()
                .getBytes("UTF-8"));
        if (recipient.getTransport().getSenders() == null
            || recipient.getTransport().getSenders().size() > 1) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "No sender is related to document or more than one sender is related!");
        }
        Organisation sendeOrg = recipient.getTransport().getSenders().get(0).getOrganisation();
        InternalXroadMember senderMember = null;
        try {
          senderMember =
              addressService.getClientForMemberCode(sendeOrg.getRegistrationCode(),
                  sendeOrg.getSubSystem());
        } catch (DhxException ex) {
          log.debug(
              "Erro occured while searching org. ignoring error and continue!" + ex.getMessage(),
              ex);
        }
        Organisation recipientOrg = recipient.getOrganisation();
        InternalXroadMember recipientMember =
            addressService.getClientForMemberCode(recipientOrg.getRegistrationCode(),
                recipientOrg.getSubSystem());
        OutgoingDhxPackage dhxPackage = null;
        // if sender org is null, then try sending with sender from config
        if (senderMember != null) {
          dhxPackage =
              dhxPackageProviderService.getOutgoingPackage(containerStream, recipient
                  .getRecipientId().toString(), recipientMember, senderMember);
        }
        else {
          dhxPackage =
              dhxPackageProviderService.getOutgoingPackage(containerStream, recipient
                  .getRecipientId().toString(), recipientMember);
        }
        recipient.setDhxInternalConsignmentId(recipient.getRecipientId().toString());
        recipient.setSendingStart(new Timestamp((new Date()).getTime()));
        asyncDhxPackageService.sendPackage(dhxPackage);
      } catch (DhxException | UnsupportedEncodingException ex) {
        log.error("Error occured while sending document! " + ex.getMessage(), ex);
        Classificator classificator = classificatorRepository.findByName(StatusEnum.FAILED.getClassificatorName());
        recipient.setStatus(classificator);
      } finally {
        FileUtil.safeCloseStream(containerStream);
        recipientRepository.save(recipient);
      }
    }
  }
}

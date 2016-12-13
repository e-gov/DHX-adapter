package ee.ria.dhx.server.persistence.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.Sender;
import ee.ria.dhx.server.persistence.entity.Transport;
import ee.ria.dhx.server.persistence.enumeration.RecipientStatusEnum;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.ObjectFactory;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Class is intended to perform actions on capsule, e.g. create capsule from persisted object and
 * backwards. Class is aware of capsule versions, and designed to be easily modified to support
 * other capsule versions aswell. Other classes in that module are unaware of capsule versions and
 * only use that class to get or set data to capsule.
 * 
 * @author Aleksei Kokarev
 *
 */

@Service
@Slf4j
public class CapsuleService {

  @Autowired
  @Setter
  CapsuleConfig capsuleConfig;

  @Value("${dhx.server.treat-cantainer-as-string}")
  @Setter
  Boolean treatContainerAsString;

  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  @Setter
  OrganisationRepository organisationRepository;

  @Autowired
  @Setter
  PersistenceService persistenceService;


  /**
   * Methods creates Document object from IncomingDhxPackage. Created object is not saved in
   * database. If document senders's organisation is not found, it is created and saved.
   * 
   * @param pckg - IncomingDhxPackage to create Document for
   * @param version - version of the capsule veing received
   * @return - Document created from IncomingDhxPackage
   * @throws DhxException
   */
  public Document getDocumentFromIncomingContainer(IncomingDhxPackage pckg,
      CapsuleVersionEnum version)
      throws DhxException {
    if (pckg.getParsedContainerVersion() != null
        && version != null && !version.equals(pckg.getParsedContainerVersion())) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Parsed container version and version in input are different!");
    }
    if (version == null && pckg.getParsedContainerVersion() != null) {
      version = pckg.getParsedContainerVersion();
    }
    if (version == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Version of the capsule is not provided!");
    }
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    String folderName = null;
    if (pckg.getDocumentFile() == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    try {
      Object container = null;
      String containerString = null;
      log.debug("creating container for incoming document");
      schemaStream = FileUtil.getFileAsStream(capsuleConfig
          .getXsdForVersion(capsuleConfig
              .getCurrentCapsuleVersion()));
      capsuleStream = pckg.getDocumentFile().getInputStream();
      if (pckg.getParsedContainer() != null) {
        container = pckg.getParsedContainer();
      }
      // TODO: think of the alternative to reading into string
      if (treatContainerAsString) {
        containerString = WsUtil.readInput(capsuleStream);
        if (container == null) {
          stringStream = new ByteArrayInputStream(containerString.getBytes("UTF-8"));
          container =
              dhxMarshallerService.unmarshallAndValidate(stringStream,
                  schemaStream);
        }
      } else if (container == null) {
        container =
            dhxMarshallerService
                .unmarshallAndValidate(capsuleStream, schemaStream);
      }
      folderName = getFolderNameFromCapsule(container);
      Document document = new Document();
      document.setCapsuleVersion(version.toString());
      DhxOrganisation dhxSenderOrg =
          DhxOrganisationFactory.createDhxOrganisation(pckg.getClient());
      Organisation senderOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
              dhxSenderOrg.getSystem());
      if (senderOrg == null) {
        if (pckg.getClient().getRepresentee() != null) {
          Organisation representor =
              persistenceService.getOrganisationFromInternalXroadMemberAndSave(pckg.getClient(),
                  true, false);
        }
        senderOrg =
            persistenceService.getOrganisationFromInternalXroadMemberAndSave(pckg.getClient(),
                false, false);
        // throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
        // "Unable to find senders organisation");
      }
      document.setContent(containerString);
      document.setOrganisation(senderOrg);
      // document.setContent(containerString);
      Integer inprocessStatusId = StatusEnum.IN_PROCESS.getClassificatorId();
      Transport transport = new Transport();
      transport.setStatusId(inprocessStatusId);
      transport.setSendingStart(new Timestamp(new Date().getTime()));
      document.addTransport(transport);
      Sender sender = new Sender();
      transport.addSender(sender);
      sender.setOrganisation(senderOrg);
      sender.setTransport(transport);
      CapsuleAdressee capsuleSender = capsuleConfig.getSenderFromContainer(container);
      sender.setPersonalCode(capsuleSender.getPersonalCode());
      sender.setStructuralUnit(capsuleSender.getStructuralUnit());
      Folder folder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
      document.setFolder(folder);
      document.setOutgoingDocument(false);
      Recipient recipient = new Recipient();
      recipient.setTransport(transport);
      recipient.setStatusId(inprocessStatusId);
      recipient.setDhxExternalConsignmentId(pckg.getExternalConsignmentId());
      recipient.setSendingStart(new Timestamp(new Date().getTime()));
      recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
      recipient.setRecipientStatusId(RecipientStatusEnum.ACCEPTED.getClassificatorId());
      DhxOrganisation dhxRecipientOrg =
          DhxOrganisationFactory.createDhxOrganisation(pckg.getService());
      Organisation recipientOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
              dhxRecipientOrg.getSystem());
      if (recipientOrg == null) {
        throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
            "Unable to find recipients organisation");
      }
      recipient.setOrganisation(recipientOrg);
      for (CapsuleAdressee containerRecipient : capsuleConfig
          .getAdresseesFromContainer(container)) {
        if (dhxRecipientOrg.equalsToCapsuleOrganisation(containerRecipient.getAdresseeCode())) {
          recipient.setPersonalcode(containerRecipient.getPersonalCode());
          recipient.setStructuralUnit(containerRecipient.getStructuralUnit());
          break;
        }
      }
      transport.addRecipient(recipient);
      return document;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting or unpacking attachment");
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(schemaStream);
      FileUtil.safeCloseStream(stringStream);
    }
  }


  /**
   * Methods creates Document object using data found from SOAP sender, SOAP recipient, container
   * and folderName. Created object is not saved in database. If document senders's organisation is
   * not found, it is created and saved. Method is meant to be used from NOT DHX services, means
   * that document will be outgoing.
   * 
   * @param senderMember - sender of the document(from SOAP header)
   * @param recipientMember - recipientMember of the document(from SOAP header)
   * @param containerHandler - container
   * @param folderName - name of the folder to save the document to
   * @param version - version of the capsule being sent
   * @return - created Document object
   * @throws DhxException
   */
  public Document getDocumentFromOutgoingContainer(
      InternalXroadMember senderMember, InternalXroadMember recipientMember,
      DataHandler containerHandler, String folderName, CapsuleVersionEnum version)
      throws DhxException {
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    if (containerHandler == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    try {
      Object container = null;
      String containerString = null;
      log.debug("creating container for outgoing document");
      schemaStream = FileUtil.getFileAsStream(capsuleConfig
          .getXsdForVersion(capsuleConfig
              .getCurrentCapsuleVersion()));
      capsuleStream =
          WsUtil.base64decodeAndUnzip(containerHandler.getInputStream());
      // TODO: think of the alternative to reading into string
      if (treatContainerAsString) {
        containerString = WsUtil.readInput(capsuleStream);
        if (container == null) {
          stringStream = new ByteArrayInputStream(containerString.getBytes("UTF-8"));
          container =
              dhxMarshallerService.unmarshallAndValidate(stringStream,
                  schemaStream);
        }
      } else if (container == null) {
        container =
            dhxMarshallerService
                .unmarshallAndValidate(capsuleStream, schemaStream);
      }
      if (folderName == null) {
        folderName = getFolderNameFromCapsule(container);
      }
      Document document = new Document();
      document.setCapsuleVersion(version.toString());
      DhxOrganisation dhxSenderOrg = DhxOrganisationFactory.createDhxOrganisation(senderMember);
      Organisation senderOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
              dhxSenderOrg.getSystem());
      if (senderOrg == null) {
        if (senderMember.getRepresentee() != null) {
          Organisation representor =
              persistenceService.getOrganisationFromInternalXroadMemberAndSave(senderMember,
                  true, false);
        }
        senderOrg =
            persistenceService.getOrganisationFromInternalXroadMemberAndSave(senderMember, false,
                false);
        // throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
        // "Unable to find senders organisation");
      }
      document.setContent(containerString);
      document.setOrganisation(senderOrg);
      // document.setContent(containerString);
      Integer inprocessStatusId = StatusEnum.IN_PROCESS.getClassificatorId();
      Transport transport = new Transport();
      transport.setStatusId(inprocessStatusId);
      transport.setSendingStart(new Timestamp(new Date().getTime()));
      document.addTransport(transport);
      Sender sender = new Sender();
      transport.addSender(sender);
      sender.setOrganisation(senderOrg);
      sender.setTransport(transport);
      CapsuleAdressee capsuleSender = capsuleConfig.getSenderFromContainer(container);
      sender.setPersonalCode(capsuleSender.getPersonalCode());
      sender.setStructuralUnit(capsuleSender.getStructuralUnit());
      Folder folder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
      document.setFolder(folder);
      document.setOutgoingDocument(true);
      for (CapsuleAdressee containerRecipient : capsuleConfig
          .getAdresseesFromContainer(container)) {
        Recipient recipient = new Recipient();
        recipient.setTransport(transport);
        recipient.setStructuralUnit(containerRecipient.getStructuralUnit());
        recipient.setStatusId(inprocessStatusId);
        recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
        recipient.setRecipientStatusId(RecipientStatusEnum.ACCEPTED.getClassificatorId());
        recipient.setPersonalcode(containerRecipient.getPersonalCode());
        recipient.setSendingStart(new Timestamp(new Date().getTime()));
        Organisation org = persistenceService.findOrg(containerRecipient.getAdresseeCode());
        if (org == null) {
          throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
              "Unable to find recipients organisation");
        }
        recipient.setOrganisation(org);
        transport.addRecipient(recipient);
      }
      return document;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting or unpacking attachment");
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(schemaStream);
      FileUtil.safeCloseStream(stringStream);
    }
  }


  /**
   * Method creates container object from Document object. Also it sets DecMetadata for capsule
   * version 2.1.
   * 
   * @param doc - Document to create DecContaner from
   * @return - created container object
   * @throws DhxException
   */
  public Object getContainerFromDocument(Document doc) throws DhxException {
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    Object container = null;
    try {
      if (treatContainerAsString) {
        stringStream = new ByteArrayInputStream(doc.getContent().getBytes("UTF-8"));
        container =
            dhxMarshallerService.unmarshallAndValidate(stringStream,
                schemaStream);
        setDecMetadataFromDocument(container, doc);
      } else {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "UNIMPLEMENTED!");
      }
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting or unpacking attachment" + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(schemaStream);
      FileUtil.safeCloseStream(stringStream);
    }
    return container;
  }

  private String getFolderNameFromCapsule(Object containerObject) throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum
        .forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getDecMetadata() != null
            && container.getDecMetadata().getDecFolder() != null) {
          return container.getDecMetadata().getDecFolder();
        }
        return null;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:"
                + version.toString());
    }
  }


  private void setDecMetadataFromDocument(Object containerObject, Document doc)
      throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum
        .forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container.getDecMetadata() == null) {
          ObjectFactory factory = new ObjectFactory();
          container.setDecMetadata(factory.createDecContainerDecMetadata());
        }
        container.getDecMetadata().setDecId(BigInteger.valueOf(doc.getDocumentId()));
        if (doc.getFolder() != null) {
          container.getDecMetadata().setDecFolder(doc.getFolder().getName());
        }
        XMLGregorianCalendar date = ConversionUtil.toGregorianCalendar(new Date());
        container.getDecMetadata().setDecReceiptDate(date);
        break;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:"
                + version.toString());
    }
  }


}

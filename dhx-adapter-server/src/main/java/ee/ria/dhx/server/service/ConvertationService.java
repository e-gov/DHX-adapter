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
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.ObjectFactory;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

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
import java.util.GregorianCalendar;

import javax.activation.DataHandler;
import javax.websocket.server.ServerEndpoint;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;


@Slf4j
@Service
public class ConvertationService {

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

  private final String DEFAULT_FOLDERNAME = "/";


  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member)
      throws DhxException {
    return getOrganisationFromInternalXroadMember(member, false);
  }

  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member,
      Boolean representorOnly)
      throws DhxException {
    Boolean newMember = false;
    Organisation organisation =
        organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
            member.getSubsystemCode());
    if (organisation == null) {
      newMember = true;
      organisation = new Organisation();
    }
    organisation.setIsActive(true);
    organisation.setMemberClass(member.getMemberClass());
    organisation.setName(member.getName());
    organisation.setRegistrationCode(member.getMemberCode());
    organisation.setSubSystem(member.getSubsystemCode());
    organisation.setXroadInstance(member.getXroadInstance());
    organisation.setDhxOrganisation(true);
    if (member.getRepresentee() != null && !representorOnly) {
      if (newMember) {
        // we cannot insert new representor with representee
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Trying to insert representee, but representor is not in database! ");
      }
      Organisation representeeOrganisation =
          organisationRepository.findByRegistrationCodeAndSubSystem(member.getRepresentee()
              .getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem());
      if (representeeOrganisation == null) {
        representeeOrganisation = new Organisation();
      }
      representeeOrganisation.setIsActive(true);
      representeeOrganisation.setName(member.getRepresentee().getRepresenteeName());
      representeeOrganisation.setRegistrationCode(member.getRepresentee().getRepresenteeCode());
      representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
      if (member.getRepresentee().getStartDate() != null) {
        representeeOrganisation.setRepresenteeStart(new Timestamp(member.getRepresentee()
            .getStartDate().getTime()));
      }
      if (member.getRepresentee().getEndDate() != null) {
        representeeOrganisation.setRepresenteeEnd(new Timestamp(member.getRepresentee()
            .getEndDate().getTime()));
      }
      representeeOrganisation.setRepresentor(organisation);
      representeeOrganisation.setDhxOrganisation(true);
      // organisation.addRepresentee(representeeOrganisation);
      organisation = representeeOrganisation;
    }
    return organisation;
  }

  public Document getDocumentFromIncomingContainer(IncomingDhxPackage document)
      throws DhxException {
    if (document.getParsedContainerVersion() != null
        && !document.getParsedContainerVersion().equals(CapsuleVersionEnum.V21)) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Only capsule version 2.1 is supported. Got:"
              + document.getParsedContainerVersion().toString());
    }
    Document doc =
        getDocumentFromContainer(document.getDocumentFile(), document.getClient(),
            document.getService(), null, false, document.getExternalConsignmentId(),
            (DecContainer) document.getParsedContainer());
    return doc;
  }


  public Document getDocumentFromOutgoingContainer(
      InternalXroadMember senderMember, InternalXroadMember recipientMember,
      DataHandler containerHandler, String folderName) throws DhxException {
    Document doc =
        getDocumentFromContainer(containerHandler, senderMember, recipientMember, folderName,
            true, null, null);
    return doc;
  }


  private Document getDocumentFromContainer(DataHandler containerHandler,
      InternalXroadMember senderMember, InternalXroadMember recipientMember, String folderName,
      Boolean outgoing, String externalConsignmentId, DecContainer parsedContainer)
      throws DhxException {
    // TODO: try to refactor this method
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    if (containerHandler == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    try {
      DecContainer container = null;
      String containerString = null;
      if (outgoing) {
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        capsuleStream =
            AttachmentUtil.base64decodeAndUnzip(containerHandler.getInputStream());
        if (parsedContainer != null) {
          container = parsedContainer;
        }
        // TODO: think of the alternative to reading into string
        if (treatContainerAsString) {
          containerString = AttachmentUtil.readInput(capsuleStream);
          if (container == null) {
            stringStream = new ByteArrayInputStream(containerString.getBytes("UTF-8"));
            container =
                (DecContainer) dhxMarshallerService.unmarshallAndValidate(stringStream,
                    schemaStream);
          }
        } else if (container == null) {
          container =
              (DecContainer) dhxMarshallerService
                  .unmarshallAndValidate(capsuleStream, schemaStream);
        }
      } else {
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        capsuleStream = containerHandler.getInputStream();
        if (parsedContainer != null) {
          container = parsedContainer;
        }
        // TODO: think of the alternative to reading into string
        if (treatContainerAsString) {
          containerString = AttachmentUtil.readInput(capsuleStream);
          if (container == null) {
            stringStream = new ByteArrayInputStream(containerString.getBytes("UTF-8"));
            container =
                (DecContainer) dhxMarshallerService.unmarshallAndValidate(stringStream,
                    schemaStream);
          }
        } else if (container == null) {
          container =
              (DecContainer) dhxMarshallerService
                  .unmarshallAndValidate(capsuleStream, schemaStream);
        }
      }
      if (folderName == null && container.getDecMetadata() != null) {
        folderName = container.getDecMetadata().getDecFolder();
      }
      Document document = new Document();
      DhxOrganisation dhxSenderOrg = DhxOrganisationFactory.createDhxOrganisation(senderMember);
      Organisation senderOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
              dhxSenderOrg.getSystem());
      if (senderOrg == null) {
        if (senderMember.getRepresentee() != null) {
          Organisation representor = getOrganisationFromInternalXroadMember(senderMember, true);
          if (representor.getOrganisationId() == null) {
            representor.setDhxOrganisation(false);
          }
          organisationRepository.save(representor);
        }
        Organisation org = getOrganisationFromInternalXroadMember(senderMember);
        org.setDhxOrganisation(false);
        organisationRepository.save(org);
        // throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
        // "Unable to find senders organisation");
      }
      document.setContent(containerString);
      document.setOrganisation(senderOrg);
      // document.setContent(containerString);
      Transport transport = new Transport();
      document.addTransport(transport);
      Sender sender = new Sender();
      transport.addSender(sender);
      sender.setOrganisation(senderOrg);
      sender.setTransport(transport);
      Classificator inprocessStatus =
          classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
      if (outgoing) {
        Folder folder = getFolderByNameOrDefaultFolder(folderName);
        document.setFolder(folder);
        document.setOutgoingDocument(true);
        for (DecRecipient containerRecipient : container.getTransport().getDecRecipient()) {
          Recipient recipient = new Recipient();
          recipient.setTransport(transport);
          recipient.setStruCturalUnit(containerRecipient.getStructuralUnit());
          recipient.setStatus(inprocessStatus);
          // recipient.setRecipientStatus(recipientStatus);
          recipient.setPersonalcode(containerRecipient.getPersonalIdCode());
          /*
           * DhxOrganisation dhxRecipientOrg =
           * getOrganisationByContainerRecipient(containerRecipient); Organisation recipientOrg =
           * organisationRepository.findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
           * dhxRecipientOrg.getSystem()); if (recipientOrg == null) { throw new
           * DhxException(DhxExceptionEnum.WRONG_SENDER, "Unable to find recipients organisation");
           * }
           */
          Organisation org = findOrg(containerRecipient.getOrganisationCode());
          if (org == null) {
            throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
                "Unable to find recipients organisation");
          }
          recipient.setOrganisation(org);
          transport.addRecipient(recipient);
        }
      } else {
        Folder folder = getFolderByNameOrDefaultFolder(folderName);
        document.setFolder(folder);
        document.setOutgoingDocument(false);
        Recipient recipient = new Recipient();
        recipient.setTransport(transport);
        recipient.setStatus(inprocessStatus);
        recipient.setDhxExternalConsignmentId(externalConsignmentId);
        // recipient.setRecipientStatus(recipientStatus);
        DhxOrganisation dhxRecipientOrg = DhxOrganisationFactory.createDhxOrganisation(recipientMember);
        Organisation recipientOrg =
            organisationRepository.findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
                dhxRecipientOrg.getSystem());
        if (recipientOrg == null) {
          throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
              "Unable to find recipients organisation");
        }
        recipient.setOrganisation(recipientOrg);
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

  private DhxOrganisation getOrganisationByContainerRecipient(DecRecipient recipient) {
    DhxOrganisation org = new DhxOrganisation();
    org.setCode(recipient.getOrganisationCode());
    return org;
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

  public Folder getFolderByNameOrDefaultFolder(String folderName) {
    if (folderName == null) {
      folderName = DEFAULT_FOLDERNAME;
    }
    Folder folder = folderRepository.findByName(folderName);
    return folder;
  }

  public DecContainer getContainerFromDocument(Document doc) throws DhxException {
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    DecContainer container = null;
    try {
      if (treatContainerAsString) {
        stringStream = new ByteArrayInputStream(doc.getContent().getBytes("UTF-8"));
        container =
            (DecContainer) dhxMarshallerService.unmarshallAndValidate(stringStream,
                schemaStream);
        if(container.getDecMetadata() == null) {
          ObjectFactory factory = new ObjectFactory();      
          container.setDecMetadata(factory.createDecContainerDecMetadata());
        }
        container.getDecMetadata().setDecId(BigInteger.valueOf(doc.getDocumentId()));
        container.getDecMetadata().setDecFolder(doc.getFolder().getName());
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        container.getDecMetadata().setDecReceiptDate(date2);
      } else {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "UNIMPLEMENTED!");
      }
    } catch (IOException | DatatypeConfigurationException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while getting or unpacking attachment" + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(capsuleStream);
      FileUtil.safeCloseStream(schemaStream);
      FileUtil.safeCloseStream(stringStream);
    }
    return container;
  }
}

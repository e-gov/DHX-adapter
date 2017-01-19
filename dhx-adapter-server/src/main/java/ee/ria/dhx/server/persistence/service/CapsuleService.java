package ee.ria.dhx.server.persistence.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.config.DhxServerConfig;
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
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.DocumentsArrayType;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.ObjectFactory;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.impl.DhxMarshallerServiceImpl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Class is intended to perform actions on capsule, e.g. create capsule from persisted object and
 * backwards. Class is aware of capsule versions, and designed to be easily modified to support
 * other capsule versions as well. Other classes in that module are unaware of capsule versions and
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


  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  @Setter
  OrganisationRepository organisationRepository;

  @Autowired
  @Setter
  PersistenceService persistenceService;

  @Autowired
  @Setter
  DhxConfig config;


  @Autowired
  @Setter
  DhxServerConfig dhxServerConfig;

  /**
   * Methods creates Document object from IncomingDhxPackage. Created object is not saved in
   * database. If document senders's organisation is not found, it is created and saved.
   * 
   * @param pckg - IncomingDhxPackage to create Document for
   * @param version - version of the capsule being received
   * @return - Document created from IncomingDhxPackage
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Document getDocumentFromIncomingContainer(IncomingDhxPackage pckg,
      CapsuleVersionEnum version)
      throws DhxException {
    if (pckg.getParsedContainerVersion() != null && version != null
        && !version.equals(pckg.getParsedContainerVersion())) {
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
    InputStream capsuleStream = null;
    String folderName = null;
    if (pckg.getDocumentFile() == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    try {
      Object container = null;
      log.debug("creating container for incoming document");
      capsuleStream = pckg.getDocumentFile().getInputStream();
      if (pckg.getParsedContainer() != null) {
        container = pckg.getParsedContainer();
      } else {
        container = dhxMarshallerService.unmarshall(capsuleStream);
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
          Organisation representor = persistenceService
              .getOrganisationFromInternalXroadMemberAndSave(pckg.getClient(), true, false);
        }
        senderOrg = persistenceService.getOrganisationFromInternalXroadMemberAndSave(
            pckg.getClient(), false,
            false);
      }
      document.setOrganisation(senderOrg);
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
      validateAndSetContainerToDocument(container, document);
      Recipient recipient = new Recipient();
      recipient.setTransport(transport);
      recipient.setStatusId(inprocessStatusId);
      recipient.setDhxExternalConsignmentId(pckg.getExternalConsignmentId());
      recipient.setSendingStart(new Timestamp(new Date().getTime()));
      recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
      recipient.setRecipientStatusId(RecipientStatusEnum.ACCEPTED.getClassificatorId());
      recipient.setOutgoing(false);
      DhxOrganisation dhxRecipientOrg = pckg.getRecipient();
      log.debug(
          "Searching recipient organisation by code: {}  system: {}", dhxRecipientOrg.getCode(),
          dhxRecipientOrg);
      Organisation recipientOrg = organisationRepository
          .findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
              dhxRecipientOrg.getSystem());
      if (recipientOrg == null) {
        throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
            "Unable to find recipients organisation. code:" + dhxRecipientOrg.getCode()
                + " system:" + dhxRecipientOrg.getSystem());
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
    }
  }

  /**
   * Method gets list of containers from attachment. Because sendDocuments service accepts many
   * containers in attachment and those containers are not wrapped with wrapper element,
   * unmarshalling does not work, therefore doing with workaround.
   * 
   * @param handler - {@link DataHandler} containing list of containers
   * @param version - {@link CapsuleVersionEnum} of the containers to parse
   * @return {@link List} of containers parsed from {@link DataHandler}
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public List<Object> getContainersList(DataHandler handler, CapsuleVersionEnum version)
      throws DhxException {
    if (handler == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    switch (version) {
      case V21:
        File tempFile = null;
        FileOutputStream fos = null;
        InputStream attachmentStream = null;
        // first try single contianer, because parsing multiple containers is more hack and does not
        // support for example container with XML declaration etc.
        try {
          log.debug("trying to parse single container in attachment.");
          attachmentStream = WsUtil.base64DecodeIfNeededAndUnzip(handler);
          DecContainer container = dhxMarshallerService.unmarshall(attachmentStream);
          log.debug("single container from attachemnt parsed");
          ArrayList<Object> containers = new ArrayList<Object>();
          containers.add(container);
          return containers;
        } catch (Exception ex) {
          log.info(
              "Got error while unmarshalling capsule. Maybe there are many capsules in reqeust. "
                  + "continue." + ex.getMessage(),
              ex);
        } finally {
          FileUtil.safeCloseStream(attachmentStream);
        }

        // if single contianer failed try to parse multiple containers.
        try {
          log.debug("trying to parse multiple containers from attachment.");
          // first create wrapper so then we can unmarshall
          tempFile = FileUtil.createPipelineFile();
          fos = new FileOutputStream(tempFile);
          attachmentStream = WsUtil.base64DecodeIfNeededAndUnzip(handler);
          FileUtil.writeToFile(
              "<DocWrapper xmlns=\"http://producers.dhl.xrd.riik.ee/producer/dhl\">", fos);
          FileUtil.writeToFile(attachmentStream, fos);
          FileUtil.writeToFile("</DocWrapper>", fos);
          fos.flush();
          FileUtil.safeCloseStream(fos);
          FileUtil.safeCloseStream(attachmentStream);
          fos = null;
          attachmentStream = null;
          DocumentsArrayType docs = null;
          if (dhxMarshallerService instanceof DhxMarshallerServiceImpl) {
            docs = ((DhxMarshallerServiceImpl) dhxMarshallerService).unmarshall(tempFile,
                DocumentsArrayType.class);
          } else {
            docs = dhxMarshallerService.unmarshall(tempFile);
          }
          List<DecContainer> containers = docs.getDecContainer();
          if (containers == null || containers.size() == 0) {
            throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
                "No container found in request.");
          }
          List<Object> objects = new ArrayList<Object>();
          objects.addAll(containers);
          log.debug("multiple containers from attachemnt parsed. total:" + objects.size());
          return objects;
        } catch (IOException ex) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "Error occured while parsing attachment." + ex.getMessage(), ex);
        } finally {
          FileUtil.safeCloseStream(attachmentStream);
          if (tempFile != null) {
            FileUtil.safeCloseStream(fos);
            tempFile.delete();
          }
        }
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:" + version.toString());
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
   * @param container - container
   * @param folderName - name of the folder to save the document to
   * @param version - version of the capsule being sent
   * @return - created Document object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Document getDocumentFromOutgoingContainer(InternalXroadMember senderMember,
      InternalXroadMember recipientMember, Object container, String folderName,
      CapsuleVersionEnum version)
      throws DhxException {
    log.debug("creating container for outgoing document");
    if (folderName == null) {
      folderName = getFolderNameFromCapsule(container);
    }
    Document document = new Document();
    document.setCapsuleVersion(version.toString());
    // set null as subsystem if provided empty string for example
    if (StringUtil.isNullOrEmpty(senderMember.getSubsystemCode())) {
      senderMember.setSubsystemCode(null);
    }
    DhxOrganisation dhxSenderOrg = DhxOrganisationFactory.createDhxOrganisation(senderMember);
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
            dhxSenderOrg.getSystem());
    // sender might not be member of DHX, means he is not in address list,
    // just sends the documents, therefore add organisation if it is not
    // found
    if (senderOrg == null) {
      log.debug(
          "sender organisation is not found in database, "
              + "need to create organiastion from InternalXroadMember.");
      if (senderMember.getRepresentee() != null) {
        Organisation representor = persistenceService
            .getOrganisationFromInternalXroadMemberAndSave(senderMember, true, false);
      }
      senderOrg = persistenceService.getOrganisationFromInternalXroadMemberAndSave(senderMember,
          false, false);
      log.debug("sender organisation is created. organisation:" + senderOrg.toString());
    }
    document.setOrganisation(senderOrg);
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
    log.debug("sender from capsule: {}", capsuleSender);
    if (config.getCheckSender()) {
      log.debug("checking if sender from capsule and sender in SOAP header are the same");
      Organisation capsuleSenderOrg = persistenceService.findOrg(capsuleSender.getAdresseeCode());
      if (!capsuleSenderOrg.equals(senderOrg)) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Request sender and capsule sender do not match! ");
      }
      log.debug("sender from capsule and sender in SOAP header are the same");
    }
    sender.setPersonalCode(capsuleSender.getPersonalCode());
    sender.setStructuralUnit(capsuleSender.getStructuralUnit());
    Folder folder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
    document.setFolder(folder);
    document.setOutgoingDocument(true);
    for (CapsuleAdressee containerRecipient : capsuleConfig
        .getAdresseesFromContainer(container)) {
      log.debug("recipient from capsule: {}", containerRecipient);
      Recipient recipient = new Recipient();
      recipient.setTransport(transport);
      recipient.setOutgoing(true);
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
      log.debug("Found recipient organisation: {}/{}", org.getRegistrationCode(),
          org.getSubSystem());
      recipient.setOrganisation(org);
      transport.addRecipient(recipient);
    }
    validateAndSetContainerToDocument(container, document);
    return document;
  }

  @Loggable
  private void validateAndSetContainerToDocument(Object container, Document document)
      throws DhxException {
    InputStream schemaStream = null;
    try {
      setDecMetadataFromDocument(container, document);
      if (config.getCapsuleValidate()) {
        log.debug("validating document.");
        schemaStream = FileUtil
            .getFileAsStream(
                capsuleConfig.getXsdForVersion(capsuleConfig.getCurrentCapsuleVersion()));
      }
      File docFile = dhxServerConfig.createDocumentFile();
      dhxMarshallerService.marshall(container, docFile);
      document.setContent(docFile.getName());
      /*
       * StringWriter writer = dhxMarshallerService.marshallToWriterAndValidate(container,
       * schemaStream);
       */
      // document.setContent(writer.toString());
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
  }


  /**
   * Method creates container object from Document object. Also it sets DecMetadata for capsule
   * version 2.1.
   * 
   * @param doc - Document to create DecContaner from
   * @return - created container object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Object getContainerFromDocument(Document doc) throws DhxException {
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    Object container = null;
    try {
      File file = dhxServerConfig.getDocumentFile(doc.getContent());
      stringStream = new FileInputStream(file);
      // String cps = WsUtil.readInput(stringStream);
      // log.debug("trying to unmarshall capsule: " + cps);
      container = dhxMarshallerService.unmarshallAndValidate(stringStream, schemaStream);
      setDecMetadataFromDocument(container, doc);
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

  /**
   * Sometimes DHX addressee(incoming document) and DVK addresse(outgoing document might be
   * different. In DHX there must be always registration code, in DVK there might be system also.
   * That method changes recipient and sender in capsule accordingly.
   * 
   * @param containerObject container Object to do changes in
   * @param sender sender organisation
   * @param recipient recipient organisation
   * @param outgoingContainer defines wether it is incoming or outgoing container.
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public void formatCapsuleRecipientAndSender(Object containerObject, Organisation sender,
      Organisation recipient,
      Boolean outgoingContainer) throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container != null) {
          String senderOraganisationCode = null;
          String recipientOrganisationCode = null;
          String recipientOrganisationCodeToFind = null;
          if (outgoingContainer) {
            senderOraganisationCode = sender.getRegistrationCode();
            recipientOrganisationCode = recipient.getRegistrationCode();
            recipientOrganisationCodeToFind = persistenceService
                .toDvkCapsuleAddressee(recipient.getRegistrationCode(), recipient.getSubSystem());
          } else {
            senderOraganisationCode =
                persistenceService.toDvkCapsuleAddressee(sender.getRegistrationCode(),
                    sender.getSubSystem());
            recipientOrganisationCode = persistenceService
                .toDvkCapsuleAddressee(recipient.getRegistrationCode(), recipient.getSubSystem());
            recipientOrganisationCodeToFind = recipient.getRegistrationCode();
          }
          log.debug("senderOraganisationCode:" + senderOraganisationCode
              + " recipientOrganisationCode:" + recipientOrganisationCode);
          container.getTransport().getDecSender().setOrganisationCode(senderOraganisationCode);
          for (DecRecipient decRecipient : container.getTransport().getDecRecipient()) {
            if (decRecipient.getOrganisationCode().equals(recipientOrganisationCodeToFind)) {
              decRecipient.setOrganisationCode(recipientOrganisationCode);
              break;
            }
          }
        }
        break;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:" + version.toString());
    }
  }

  /**
   * When all actions with contianer are complete, delete file which might be related to the
   * container.
   * 
   * @param containerObject container to cleanup
   * @throws DhxException thrown when error occurs
   */
  @Loggable
  public void cleanupContainer(Object containerObject) throws DhxException {
    if (containerObject == null) {
      return;
    }
    CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getFile() != null) {
          for (ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.File decFile : container
              .getFile()) {
            if (decFile.getZipBase64Content() != null) {
              decFile.getZipBase64Content().delete();
            }
          }
        }
        break;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to cleanup container of given version. version:" + version.toString());
    }
  }

  /**
   * When all actions with contianer are complete, delete file which might be related to the
   * container.
   * @param containers list of container to cleanup
   * @throws DhxException thrown if error occurs
   */
  public void cleanupContainers(List<? extends Object> containers) throws DhxException {
    for (Object obj : containers) {
      cleanupContainer(obj);
    }
  }

  @Loggable
  private String getFolderNameFromCapsule(Object containerObject) throws DhxException {
    if (containerObject == null) {
      return null;
    }
    CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
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
            "Unable to find adressees for given verion. version:" + version.toString());
    }
  }

  @Loggable
  private void setDecMetadataFromDocument(Object containerObject, Document doc)
      throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum.forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container.getDecMetadata() == null) {
          log.debug("creating DecMetadata");
          ObjectFactory factory = new ObjectFactory();
          container.setDecMetadata(factory.createDecContainerDecMetadata());
        }
        if (doc.getDocumentId() != null) {
          log.debug("creating DocumentId: " + doc.getDocumentId());
          container.getDecMetadata().setDecId(BigInteger.valueOf(doc.getDocumentId()));
        } else if (container.getDecMetadata().getDecId() == null) {
          log.debug("in order capsule to validate, setting random value as document id");
          // set random just to validate
          container.getDecMetadata().setDecId(BigInteger.valueOf(99999));
        }
        if (doc.getFolder() != null) {
          log.debug("creating Folder: " + doc.getFolder());
          container.getDecMetadata().setDecFolder(doc.getFolder().getName());
        }
        XMLGregorianCalendar date = ConversionUtil.toGregorianCalendar(new Date());
        container.getDecMetadata().setDecReceiptDate(date);
        break;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:" + version.toString());
    }
  }

}

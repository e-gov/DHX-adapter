package ee.ria.dhx.server.persistence.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.Sender;
import ee.ria.dhx.server.persistence.entity.Transport;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.service.ConvertationService;
import ee.ria.dhx.server.service.util.StatusEnum;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.ObjectFactory;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
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
  CapsuleConfig capsuleConfig;

  @Value("${dhx.server.treat-cantainer-as-string}")
  Boolean treatContainerAsString;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  OrganisationRepository organisationRepository;
  
  @Autowired
  PersistenceService persistenceService;


  /**
   * Methods creates Document object from IncomingDhxPackage. Created object is not saved in
   * database. If document senders's organisation is not found, it is created and saved.
   * 
   * @param document - IncomingDhxPackage to create Document for
   * @param version - version of the capsule veing received
   * @return - Document created from IncomingDhxPackage
   * @throws DhxException
   */
  public Document getDocumentFromIncomingContainer(IncomingDhxPackage document,
      CapsuleVersionEnum version)
      throws DhxException {
    if (document.getParsedContainerVersion() != null
        && version != null && !version.equals(document.getParsedContainerVersion())) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Parsed container version and version in input are different!");
    }
    if( version == null && document.getParsedContainerVersion() != null) {
      version = document.getParsedContainerVersion();
    }
    if(version == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Version of the capsule is not provided!");
    }
    Document doc =
        getDocumentFromContainer(document.getDocumentFile(), document.getClient(),
            document.getService(), null, false, document.getExternalConsignmentId(),
            document.getParsedContainer(), version);
    return doc;
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
    Document doc =
        getDocumentFromContainer(containerHandler, senderMember, recipientMember, folderName,
            true, null, null, version);
    return doc;
  }


  /**
   * Method creates container object from Document object.
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
        container.getDecMetadata().setDecFolder(doc.getFolder().getName());
        XMLGregorianCalendar date = ConversionUtil.toGregorianCalendar(new Date());
        container.getDecMetadata().setDecReceiptDate(date);
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:"
                + version.toString());
    }
  }

  private Document getDocumentFromContainer(DataHandler containerHandler,
      InternalXroadMember senderMember, InternalXroadMember recipientMember, String folderName,
      Boolean outgoing, String externalConsignmentId, Object parsedContainer,
      CapsuleVersionEnum capsuleVersion)
      throws DhxException {
    // TODO: try to refactor this method
    InputStream schemaStream = null;
    InputStream capsuleStream = null;
    InputStream stringStream = null;
    if (containerHandler == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Empty attachment!");
    }
    try {
      Object container = null;
      String containerString = null;
      if (outgoing) {
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        capsuleStream =
            WsUtil.base64decodeAndUnzip(containerHandler.getInputStream());
        if (parsedContainer != null) {
          container = parsedContainer;
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
      }
      if (folderName == null) {
        folderName = getFolderNameFromCapsule(container);
      }
      Document document = new Document();
      document.setCapsuleVersion(capsuleVersion.toString());
      DhxOrganisation dhxSenderOrg = DhxOrganisationFactory.createDhxOrganisation(senderMember);
      Organisation senderOrg =
          organisationRepository.findByRegistrationCodeAndSubSystem(dhxSenderOrg.getCode(),
              dhxSenderOrg.getSystem());
      if (senderOrg == null) {
        if (senderMember.getRepresentee() != null) {
          Organisation representor =
              persistenceService.getOrganisationFromInternalXroadMember(senderMember, true);
          if (representor.getOrganisationId() == null) {
            representor.setDhxOrganisation(false);
          }
          organisationRepository.save(representor);
        }
        Organisation org =
            persistenceService.getOrganisationFromInternalXroadMember(senderMember);
        org.setDhxOrganisation(false);
        organisationRepository.save(org);
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

      if (outgoing) {
        Folder folder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
        document.setFolder(folder);
        document.setOutgoingDocument(true);
        for (CapsuleAdressee containerRecipient : capsuleConfig
            .getAdresseesFromContainer(container)) {
          Recipient recipient = new Recipient();
          recipient.setTransport(transport);
          recipient.setStruCturalUnit(containerRecipient.getStructuralUnit());
          recipient.setStatusId(inprocessStatusId);
          recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
          // recipient.setRecipientStatus(recipientStatus);
          recipient.setPersonalcode(containerRecipient.getPersonalCode());
          recipient.setSendingStart(new Timestamp(new Date().getTime()));
          /*
           * DhxOrganisation dhxRecipientOrg =
           * getOrganisationByContainerRecipient(containerRecipient); Organisation recipientOrg =
           * organisationRepository.findByRegistrationCodeAndSubSystem(dhxRecipientOrg.getCode(),
           * dhxRecipientOrg.getSystem()); if (recipientOrg == null) { throw new
           * DhxException(DhxExceptionEnum.WRONG_SENDER, "Unable to find recipients organisation");
           * }
           */
          Organisation org = persistenceService.findOrg(containerRecipient.getAdresseeCode());
          if (org == null) {
            throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
                "Unable to find recipients organisation");
          }
          recipient.setOrganisation(org);
          transport.addRecipient(recipient);
        }
      } else {
        Folder folder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
        document.setFolder(folder);
        document.setOutgoingDocument(false);
        Recipient recipient = new Recipient();
        recipient.setTransport(transport);
        recipient.setStatusId(inprocessStatusId);
        recipient.setDhxExternalConsignmentId(externalConsignmentId);
        recipient.setSendingStart(new Timestamp(new Date().getTime()));
        recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
        // recipient.setRecipientStatus(recipientStatus);
        DhxOrganisation dhxRecipientOrg =
            DhxOrganisationFactory.createDhxOrganisation(recipientMember);
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



}

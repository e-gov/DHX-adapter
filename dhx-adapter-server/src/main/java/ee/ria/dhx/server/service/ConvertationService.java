package ee.ria.dhx.server.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.entity.Document;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.entity.Recipient;
import ee.ria.dhx.server.entity.Sender;
import ee.ria.dhx.server.entity.Transport;
import ee.ria.dhx.server.repository.DocumentRepository;
import ee.ria.dhx.server.repository.FolderRepository;
import ee.ria.dhx.server.repository.OrganisationRepository;
import ee.ria.dhx.server.repository.RecipientRepository;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.server.service.util.StatusEnum;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.ObjectFactory;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Class for converting between SOAP objects and Database obejcts.
 * 
 * @author Aleksei Kokarev
 *
 */

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


  /**
   * Method finds or creates new Organisation object according to data from InternalXroadMember. If
   * object was not found in database, new object is created but not saved to database.
   * 
   * @param member - InternalXroadMember to find Organisation for
   * @return - created or found Organisation
   * @throws DhxException
   */
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


  /**
   * Methods creates Document object from IncomingDhxPackage. Created object is not saved in
   * database. If document senders's organisation is not found, it is created and saved.
   * 
   * @param document - IncomingDhxPackage to create Document for
   * @return - Document created from IncomingDhxPackage
   * @throws DhxException
   */
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
   * @return - created Document object
   * @throws DhxException
   */
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
          containerString = WsUtil.readInput(capsuleStream);
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
        Folder folder = getFolderByNameOrDefaultFolder(folderName);
        document.setFolder(folder);
        document.setOutgoingDocument(true);
        for (DecRecipient containerRecipient : container.getTransport().getDecRecipient()) {
          Recipient recipient = new Recipient();
          recipient.setTransport(transport);
          recipient.setStruCturalUnit(containerRecipient.getStructuralUnit());
          recipient.setStatusId(inprocessStatusId);
          recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
          // recipient.setRecipientStatus(recipientStatus);
          recipient.setPersonalcode(containerRecipient.getPersonalIdCode());
          recipient.setSendingStart(new Timestamp(new Date().getTime()));
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
  }

  /**
   * Finds folder according to folderName, or by default folder name if folderName in input is NULL.
   * 
   * @param folderName - name of the folder to find
   * @return - Folder object found
   */
  public Folder getFolderByNameOrDefaultFolder(String folderName) {
    if (folderName == null) {
      folderName = DEFAULT_FOLDERNAME;
    }
    Folder folder = folderRepository.findByName(folderName);
    return folder;
  }

  /**
   * Method creates DecContainer object from Document object.
   * @param doc - Document to create DecContaner from
   * @return - created DecContainer
   * @throws DhxException
   */
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
        if (container.getDecMetadata() == null) {
          ObjectFactory factory = new ObjectFactory();
          container.setDecMetadata(factory.createDecContainerDecMetadata());
        }
        container.getDecMetadata().setDecId(BigInteger.valueOf(doc.getDocumentId()));
        container.getDecMetadata().setDecFolder(doc.getFolder().getName());
        XMLGregorianCalendar date = WsUtil.getXmlGregorianCalendarFromDate(new Date());
        container.getDecMetadata().setDecReceiptDate(date);
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
  
  
  
  public DataHandler createDatahandlerFromObject(Object obj) throws DhxException{
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = WsUtil.getBase64EncodeStream(fos);
      zippedStream = WsUtil.getGZipCompressStream(base64Stream);
      dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(obj, zippedStream);
      zippedStream.finish();
      base64Stream.flush();
      fos.flush();
      DataSource datasource = new FileDataSource(file);
      return new DataHandler(datasource);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }
  }
  
  public DataHandler createDatahandlerFromList(List<? extends Object> objList) throws DhxException{
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = WsUtil.getBase64EncodeStream(fos);
      zippedStream = WsUtil.getGZipCompressStream(base64Stream);
      for(Object obj : objList) {
        dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(obj, zippedStream);
      }
      zippedStream.finish();
      base64Stream.flush();
      fos.flush();
      DataSource datasource = new FileDataSource(file);
      return new DataHandler(datasource);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }
  }
}

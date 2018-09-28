package ee.ria.dhx.server.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.config.DhxServerConfig;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.StatusHistory;
import ee.ria.dhx.server.persistence.enumeration.RecipientStatusEnum;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.DocumentRepository;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.persistence.repository.RecipientRepository;
import ee.ria.dhx.server.persistence.service.CapsuleService;
import ee.ria.dhx.server.persistence.service.PersistenceService;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.server.types.ee.riik.schemas.dhl.AadressType;
import ee.ria.dhx.server.types.ee.riik.schemas.dhl.Edastus;
import ee.ria.dhx.server.types.ee.riik.schemas.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Dokumendid;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsV2RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionV3ResponseBody;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionRefsArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedV3RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ObjectFactory;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendingOptionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.StatusHistoryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.StatusHistoryType.Status;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.StatusType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.TagasisideType;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ws.context.MessageContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Class that gets SOAP request objects(from DVK services), processes requests and gives SOAP
 * response objects.
 * 
 * @author Aleksei Kokarev
 *
 */

@Service
@Slf4j
@Transactional
public class SoapService {

  @Autowired
  @Setter
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  @Setter
  FolderRepository folderRepository;

  @Autowired
  @Setter
  OrganisationRepository organisationRepository;

  @Autowired
  @Setter
  AddressService addressService;

  @Autowired
  @Setter
  DocumentRepository documentRepository;

  @Autowired
  @Setter
  RecipientRepository recipientRepository;

  @Autowired
  @Setter
  AsyncDhxPackageService asyncDhxPackageService;

  @Autowired
  @Setter
  DhxPackageProviderService dhxPackageProviderService;

  @Autowired
  @Setter
  ConvertationService convertationService;

  @Autowired
  @Setter
  CapsuleService capsuleService;

  @Autowired
  @Setter
  PersistenceService persistenceService;

  @Autowired
  @Setter
  SoapConfig soapConfig;

  @Autowired
  @Setter
  DhxServerConfig dhxServerConfig;


  @Value("${dhx.server.received-document-lifetime}")
  @Setter
  Integer receivedDocumentLifetime;

  @Value("${dhx.server.failed-document-lifetime}")
  @Setter
  Integer failedDocumentLifetime;


  @Value("${dhx.resend.timeout}")
  @Setter
  Integer resendTimeout;

  /**
   * Method processes sendDocuments request and saves the document into the database for further
   * sending to DHX service of the recipient.
   * 
   * @param documents - SOAP request object
   * @param sender - sender of the request(from SOAP header)
   * @param recipient - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public SendDocumentsResponse sendDocuments(SendDocuments documents, InternalXroadMember sender,
      InternalXroadMember recipient) throws DhxException {
    // for now support only v21
    CapsuleVersionEnum version = CapsuleVersionEnum.V21;
    List<Object> containers =
        capsuleService.getContainersList(documents.getKeha().getDokumendid().getHref(),
            version);
    ObjectFactory fact = new ObjectFactory();
    SendDocumentsResponse response = fact.createSendDocumentsResponse();
    response.setKeha(fact.createBase64BinaryType());
    SendDocumentsV4ResponseTypeUnencoded.Keha attachmentObj =
        fact.createSendDocumentsV4ResponseTypeUnencodedKeha();
    for (Object container : containers) {
      Document document =
          capsuleService.getDocumentFromOutgoingContainer(sender, recipient, container,
              documents.getKeha().getKaust(), version);
      documentRepository.save(document);
      attachmentObj.getDhlId().add(document.getDocumentId().toString());


    }
    capsuleService.cleanupContainers(containers);
    DataHandler handler = convertationService.createDatahandlerFromObject(attachmentObj);
    response.getKeha().setHref(handler);
    return response;
  }

  /**
   * Method finds not sent documents that are meant to be sent to DHX(outgoing documents) and sends
   * them using asynchronous DHX sender.
   */
  @Loggable
  public void sendDocumentsToDhx() {
    Integer statusId = StatusEnum.IN_PROCESS.getClassificatorId();
    // not sent documents
    List<Recipient> recipients = recipientRepository
        .findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNull(
            statusId, true);
    Date date = new Date();
    date.setTime(date.getTime() - resendTimeout * 1000 * 60);
    log.debug("date from which documents will be sent again: {}", date);
    // documents that tried to send, but maybe server were stopped and status stays the same
    List<Recipient> recipientsSent = recipientRepository
        .findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNotNullAndDateModifiedLessThan(
            statusId, true, date);
    recipients.addAll(recipientsSent);
    if (recipients != null) {
      log.debug("found total recipients to send to DHX: " + recipients.size());
    }
    Recipient lockedRecipient = null;
    for (Recipient recipientUnlocked : recipients) {
      try {
        // doing query for every recipient to lock it for writing, in order for another thread not
        // to update it
        lockedRecipient = recipientUnlocked;
        // if recipient were updated by another transaction, then skip
        if (lockedRecipient.getVersion() != null && recipientUnlocked.getVersion() != null
            && lockedRecipient.getVersion() != recipientUnlocked.getVersion()) {
          log.info("recipient were updated by another transaction, skiping it. {}",
              lockedRecipient);
          continue;
        }
        Document document = lockedRecipient.getTransport().getDokument();
        Object container = capsuleService.getContainerFromDocument(document);
        capsuleService.formatCapsuleRecipientAndSender(container,
            lockedRecipient.getTransport().getSenders().get(0).getOrganisation(),
            lockedRecipient.getOrganisation(),
            true);
        File containerFile = dhxMarshallerService.marshall(container);
        if (lockedRecipient.getTransport().getSenders() == null
            || lockedRecipient.getTransport().getSenders().size() > 1) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "No sender is related to document or more than one sender is related!");
        }
        Organisation sendeOrg =
            lockedRecipient.getTransport().getSenders().get(0).getOrganisation();
        InternalXroadMember senderMember = null;
        try {
          senderMember = addressService.getClientForMemberCode(sendeOrg.getRegistrationCode(),
              sendeOrg.getSubSystem());
        } catch (DhxException ex) {
          log.debug(
              "Erro occured while searching org. ignoring error and continue!" + ex.getMessage(),
              ex);
        }
        Organisation recipientOrg = lockedRecipient.getOrganisation();
        InternalXroadMember recipientMember = addressService
            .getClientForMemberCode(recipientOrg.getRegistrationCode(),
                recipientOrg.getSubSystem());
        log.debug("Found recipient member: {}", recipientMember);
        OutgoingDhxPackage dhxPackage = null;
        // if sender org is null, then try sending with sender from
        // config
        if (senderMember != null) {
          dhxPackage = dhxPackageProviderService.getOutgoingPackage(containerFile,
              lockedRecipient.getRecipientId().toString(), recipientMember, senderMember);
        } else {
          dhxPackage = dhxPackageProviderService.getOutgoingPackage(containerFile,
              lockedRecipient.getRecipientId().toString(), recipientMember);
        }
        lockedRecipient.setDhxInternalConsignmentId(lockedRecipient.getRecipientId().toString());
        lockedRecipient.setSendingStart(new Timestamp((new Date()).getTime()));
        saveRecipient(lockedRecipient);
        asyncDhxPackageService.sendPackage(dhxPackage);
      } catch (DhxException ex) {
        log.error("Error occured while sending document! " + ex.getMessage(), ex);
        Integer failedStatusId = StatusEnum.FAILED.getClassificatorId();
        if (lockedRecipient != null) {
          lockedRecipient.setStatusId(failedStatusId);
          lockedRecipient.setFaultString(ex.getMessage());
          lockedRecipient.setFaultCode(ex.getExceptionCode().toString());
          lockedRecipient.setRecipientStatusId(RecipientStatusEnum.REJECTED.getClassificatorId());
          recipientRepository.save(lockedRecipient);
        }
      }
    }
  }

  @Loggable
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void saveRecipient(Recipient recipient) {
    recipientRepository.saveAndFlush(recipient);
  }

  /**
   * Method returns documents that are sent to sender's organisation. Paramters from request are
   * also used to find documents to return.
   * 
   * @param request - SOAP request object
   * @param sender - sender of the request(from SOAP header)
   * @param recipient - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public ReceiveDocumentsResponse receiveDocuments(ReceiveDocuments request,
      InternalXroadMember sender,
      InternalXroadMember recipient) throws DhxException {
    Pageable pageable = null;
    if (request.getKeha().getArv() != null) {
      pageable = new PageRequest(0, request.getKeha().getArv().intValue());
    } else {
      pageable = new PageRequest(0, 10);
    }
    // set null as subsystem if provided empty string for example
    if (StringUtil.isNullOrEmpty(sender.getSubsystemCode())) {
      sender.setSubsystemCode(null);
    }
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(sender.getMemberCode(),
            sender.getSubsystemCode());
    log.debug("receiving document for organisation: {}", senderOrg);
    Folder folder = null;
    if (!StringUtil.isNullOrEmpty(request.getKeha().getKaust())) {
      folder = folderRepository.findByName(request.getKeha().getKaust());
    }
    Integer inprocessStatusId = StatusEnum.IN_PROCESS.getClassificatorId();
    List<Document> docs = null;
    if (folder == null && StringUtil.isNullOrEmpty(request.getKeha().getKaust())) {
      if (senderOrg != null) {
        log.debug(
            "searching by recipients organisation and status " + senderOrg.getOrganisationId());
      }
      docs = documentRepository
          .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusId(
              false,
              senderOrg, inprocessStatusId, pageable);
    } else {
      log.debug("searching by recipients organisation, folder and status");
      docs = documentRepository
          .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
              false, senderOrg, inprocessStatusId, folder, pageable);
    }
    if (docs != null) {
      log.debug("found docs: " + docs.size());
    }
    ObjectFactory fact = new ObjectFactory();
    Base64BinaryType att = fact.createBase64BinaryType();
    List<Object> containers = new ArrayList<Object>();
    for (Document doc : docs) {
      Object container = capsuleService.getContainerFromDocument(doc);
      capsuleService.formatCapsuleRecipientAndSender(container,
          doc.getTransports().get(0).getSenders().get(0).getOrganisation(), senderOrg, false);
      containers.add(container);
    }
    DataHandler handler = convertationService.createDatahandlerFromList(containers);
    capsuleService.cleanupContainers(containers);
    att.setHref(handler);
    ReceiveDocumentsResponse resp = fact.createReceiveDocumentsResponse();
    resp.setKeha(att);
    return resp;
  }

  /**
   * Because we need to support multiple different versions, therefore we need to parse the body and
   * set it to request manually.
   */
  private void setMarkDocumentsReceivedRequestBody(MarkDocumentsReceived requestWrapper,
      MessageContext context, InternalXroadMember recipientMember) throws DhxException {
    MarkDocumentsReceivedV3RequestType request = null;
    if (!StringUtil.isNullOrEmpty(requestWrapper.getKeha().getDokumendid().getHrefString())
        || requestWrapper.getKeha() != null) {
      log.debug("Found keha and dokumendid elements in request.");
      if (!recipientMember.getServiceVersion().equals("v3")) {
        log.debug("Dealing with request in attachment defined in href(v2, v1).");
        InputStream attachmentStream = null;
        FileOutputStream fos = null;
        File tempFile = null;
        try {
          tempFile = FileUtil.createPipelineFile();
          fos = new FileOutputStream(tempFile);
          DataHandler attachmentHandler = WsUtil.extractAttachment(context,
              requestWrapper.getKeha().getDokumendid().getHrefString());
          // first try extract
          if (recipientMember.getServiceVersion().equals("v2")) {
            attachmentStream = WsUtil.base64DecodeIfNeededAndUnzip(attachmentHandler);
            FileUtil.writeToFile(
                "<dokumendid>", fos);
            FileUtil.writeToFile(attachmentStream, fos);
            FileUtil.writeToFile("</dokumendid>", fos);
            fos.flush();
            FileUtil.safeCloseStream(fos);
            FileUtil.safeCloseStream(attachmentStream);
            fos = null;
            attachmentStream = null;
            Dokumendid docs = dhxMarshallerService.unmarshall(tempFile);
            request = new MarkDocumentsReceivedV3RequestType();
            request.setDokumendid(docs);
          } else if (recipientMember.getServiceVersion().equals("v1")) {
            request = new MarkDocumentsReceivedV3RequestType();
            List<TagasisideType> taagsisideList = new ArrayList<TagasisideType>();
            request.setDokumendid(new Dokumendid());
            request.getDokumendid().setTagasisided(taagsisideList);
            org.w3c.dom.Document xmlDoc = WsUtil.xmlDocumentFromStream(
                WsUtil.base64DecodeIfNeededAndUnzip(
                    attachmentHandler));
            NodeList list = xmlDoc.getElementsByTagName("dhl_id");
            for (int j = 0; j < list.getLength(); j++) {
              Node node = list.item(j);
              log.debug("dhl id " + node.getTextContent());
              TagasisideType tagasiside = new TagasisideType();
              tagasiside.setDhlId(BigInteger.valueOf(Long.valueOf(node.getTextContent())));
              taagsisideList.add(tagasiside);
            }
            if (taagsisideList.size() == 0) {
              throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
                  "No dhl ids are provided to get status for.");
            }
          }
        } catch (IOException ex) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "Error occured while parsing attachment." + ex.getMessage(), ex);
        } finally {
          FileUtil.safeCloseStream(attachmentStream);
          FileUtil.safeCloseStream(fos);
          if (tempFile != null) {
            tempFile.delete();
          }
        }
        requestWrapper.setKeha(request);
      }
    } else {
      throw new DhxException("Request is empty or invalid!");
    }
  }

  /**
   * Method marks documents with status provided in request. Documents which will be marked are
   * parameters provided in request.
   * 
   * @param requestWrapper SOAP request object
   * @param senderMember sender of the request(from SOAP header)
   * @param recipientMember recipient of the request(from SOAP header)
   * @param context SOAP context, used for attachment parsing
   * @return SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public MarkDocumentsReceivedResponse markDocumentReceived(
      MarkDocumentsReceived requestWrapper,
      InternalXroadMember senderMember, InternalXroadMember recipientMember,
      MessageContext context) throws DhxException {
    setMarkDocumentsReceivedRequestBody(requestWrapper, context, recipientMember);
    MarkDocumentsReceivedV3RequestType request = requestWrapper.getKeha();

    if (request == null || request.getDokumendid() == null
        || request.getDokumendid().getTagasisided() == null
        || request.getDokumendid().getTagasisided().size() == 0) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "No documents to mark received is provided in request.");
    }
    // set null as subsystem if provided empty string for example
    if (StringUtil.isNullOrEmpty(senderMember.getSubsystemCode())) {
      senderMember.setSubsystemCode(null);
    }
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
            senderMember.getSubsystemCode());
    Integer failedStatusId = StatusEnum.FAILED.getClassificatorId();
    Integer successStatusId = StatusEnum.RECEIVED.getClassificatorId();
    Folder folder = null;
    if (request.getKaust() != null) {
      folder = folderRepository.findByName(request.getKaust());
    }
    if (senderOrg == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Senders organisation not found. organisation:" + senderMember.toString());
    } else {
      log.debug("Marking documents received for organisation: {}", senderOrg);
    }
    Boolean allSent = true;
    Boolean allFailed = true;
    Boolean found = false;
    for (TagasisideType status : request.getDokumendid().getTagasisided()) {
      Document doc = documentRepository.findOne(status.getDhlId().longValue());
      if (log.isTraceEnabled()) {
        log.trace("marking document receiverd: {}", doc);
      }
      if (doc == null) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Document is not found.");
      }
      if (folder != null && !doc.getFolder().getFolderId().equals(folder.getFolderId())) {
        continue;
      }
      for (Recipient recipient : doc.getTransports().get(0).getRecipients()) {
        if (recipient.getOrganisation().getOrganisationId()
            .equals(senderOrg.getOrganisationId())) {
          found = true;
          if (status.getFault() != null) {
            recipient.setFaultActor(status.getFault().getFaultactor());
            recipient.setFaultCode(status.getFault().getFaultcode());
            recipient.setFaultDetail(status.getFault().getFaultdetail());
            recipient.setFaultString(status.getFault().getFaultstring());
            recipient.setStatusId(failedStatusId);
          } else {
            recipient.setStatusId(successStatusId);
          }
          if (status.getMetaxml() != null) {
            StringWriter writer = dhxMarshallerService.marshallToWriter(status.getMetaxml());
            recipient.setMetaxml(writer.toString());
          }
          if (status.getStaatuseMuutmiseAeg() != null) {
            Timestamp date = new Timestamp(
                status.getStaatuseMuutmiseAeg().toGregorianCalendar().getTime().getTime());
            recipient.setStatusChangeDate(date);
          } else {
            recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
          }
          recipient.setSendingEnd(new Timestamp(new Date().getTime()));
          if (status.getVastuvotjaStaatusId() != null) {
            recipient.setRecipientStatusId(status.getVastuvotjaStaatusId().intValue());
          }
          persistenceService.addStatusHistory(recipient);
          if (log.isTraceEnabled()) {
            log.trace("changed recipient: {}", recipient);
          }
          recipientRepository.save(recipient);
        }
        if (recipient.getStatusId() != null
            && !recipient.getStatusId().equals(StatusEnum.RECEIVED.getClassificatorId())) {
          allSent = false;
        }

        if (recipient.getStatusId() != null
            && !recipient.getStatusId().equals(StatusEnum.FAILED.getClassificatorId())) {
          allFailed = false;
        }

      }
      if (!found) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "That document is not sent to recipient organisation. dhlId:"
                + status.getDhlId().longValue()
                + " organisation:" + senderOrg.getRegistrationCode());
      }
      if (allSent && !doc.getTransports().get(0).getStatusId()
          .equals(StatusEnum.RECEIVED.getClassificatorId())) {
        log.debug(
            "all of the documwents reciepient are in status received, "
                + "setting same status to document.");
        doc.getTransports().get(0).setStatusId(successStatusId);
        documentRepository.save(doc);
      }
      if (allFailed && !doc.getTransports().get(0).getStatusId()
          .equals(StatusEnum.FAILED.getClassificatorId())) {
        log.debug(
            "all of the documwents reciepient are in status failed, "
                + "setting same status to document.");
        doc.getTransports().get(0).setStatusId(failedStatusId);
        documentRepository.save(doc);
      }
    }
    ObjectFactory factory = new ObjectFactory();
    MarkDocumentsReceivedResponse response = factory.createMarkDocumentsReceivedResponse();
    response.setKeha("OK");
    return response;
  }

  private void setGetSendStatusRequestBody(GetSendStatus requestWrapper,
      InternalXroadMember recipientMember, MessageContext context) throws DhxException {
    if (requestWrapper.getAny() != null && requestWrapper.getAny().size() > 0
        && requestWrapper.getAny().get(0).getTagName().equals("keha")) {
      Node keha = requestWrapper.getAny().get(0);
      log.debug("Found keha element in request.");
      DataHandler attachmentHandler = null;
      Boolean history = false;
      if (recipientMember.getServiceVersion().equals("v1")) {
        for (int i = 0; i < keha.getAttributes().getLength(); i++) {
          Node att = keha.getAttributes().item(i);
          if (att.getLocalName().equals("href")) {
            String content = att.getTextContent();
            log.debug("Dealing with request in attachment defined in href(v3).");
            attachmentHandler = WsUtil.extractAttachment(context, content);
          }
        }
      } else if (recipientMember.getServiceVersion().equals("v2")) {
        if (keha.getChildNodes() != null && keha.getChildNodes().getLength() > 0) {
          for (int k = 0; k < keha.getChildNodes().getLength(); k++) {
            Node node = keha.getChildNodes().item(k);
            if (node != null && node.getLocalName() != null) {
              if (node.getLocalName().equals("dokumendid")) {
                for (int i = 0; i < node.getAttributes().getLength(); i++) {
                  Node att = node.getAttributes().item(i);
                  if (att.getLocalName().equals("href")) {
                    String content = att.getTextContent();
                    attachmentHandler = WsUtil.extractAttachment(context, content);
                  }
                }
              } else if (node.getLocalName().equals("staatuse_ajalugu")) {
                history = Boolean.valueOf(node.getTextContent());
              }
            }
          }
        }
      }
      requestWrapper.setKeha(new GetSendStatusV2RequestType());
      requestWrapper.getKeha().setDokumendid(new Base64BinaryType());
      requestWrapper.getKeha().getDokumendid().setHref(attachmentHandler);
      requestWrapper.getKeha().setStaatuseAjalugu(history);
    } else {
      throw new DhxException("Request is empty or invalid!");
    }
  }

  /**
   * Method returns statuses of the documents. Documents are found by the parameters provided in
   * request.
   * 
   * @param request - SOAP request object
   * @param senderMember - sender of the request(from SOAP header)
   * @param recipientMember - recipient of the request(from SOAP header)
   * @param context Soap message context
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public GetSendStatusResponse getSendStatus(GetSendStatus request,
      InternalXroadMember senderMember,
      InternalXroadMember recipientMember, MessageContext context) throws DhxException {
    if (request.getKeha() == null) {
      setGetSendStatusRequestBody(request, recipientMember, context);
    }
    if (request.getKeha() == null || request.getKeha().getDokumendid() == null
        || request.getKeha().getDokumendid().getHref() == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Request is empty or invalid!");
    }
    org.w3c.dom.Document xmlDoc = WsUtil.xmlDocumentFromStream(
        WsUtil.base64DecodeIfNeededAndUnzip(
            request.getKeha().getDokumendid().getHref()));
    List<Long> dhlIds = new ArrayList<Long>();
    NodeList list = xmlDoc.getElementsByTagName("dhl_id");
    for (int i = 0; i < list.getLength(); i++) {
      Node node = list.item(i);
      log.debug("dhl id " + node.getTextContent());
      dhlIds.add(Long.valueOf(node.getTextContent()));
    }
    if (dhlIds.size() == 0) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "No dhl ids are provided to get status for.");
    }
    
    log.debug("getSendStatus senderMember: {}", senderMember);
    log.debug("getSendStatus recipientMember: {}", recipientMember);
    log.debug("getSendStatus recipientMember.getServiceVersion(): {}", recipientMember.getServiceVersion());
    
    if (recipientMember.getServiceVersion() == null || !recipientMember.getServiceVersion().equals("v2")) {
      list = xmlDoc.getElementsByTagName("dokument_guid");
      if (list.getLength() > 0) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Getting send status by dokument_guid is not supported");
      }
    }
    List<Document> documents = documentRepository.findByDocumentIdIn(dhlIds);
    ObjectFactory factory = new ObjectFactory();
    GetSendStatusResponse response = factory.createGetSendStatusResponse();
    response.setKeha(factory.createBase64BinaryType());
    GetSendStatusV2ResponseTypeUnencoded responseAtt =
        factory.createGetSendStatusV2ResponseTypeUnencoded();
    for (Document doc : documents) {
      if (log.isTraceEnabled()) {
        log.trace("getting send status for document: {}", doc);
      }
      GetSendStatusV2ResponseTypeUnencoded.Item item = factory
          .createGetSendStatusV2ResponseTypeUnencodedItem();
      StatusHistoryType history = null;
      if (request.getKeha().isStaatuseAjalugu()) {
        history = new StatusHistoryType();
      }
      item.setDhlId(doc.getDocumentId().toString());
      item.setOlek(
          StatusEnum.forClassificatorId(doc.getTransports().get(0).getStatusId())
              .getClassificatorName());
      for (Recipient recipient : doc.getTransports().get(0).getRecipients()) {
        Edastus edastus = new Edastus();
        if (recipient.getRecipientStatusId() != null) {
          edastus.setVastuvotjaStaatusId(BigInteger.valueOf(recipient.getRecipientStatusId()));
        }
        edastus.setStaatus(
            StatusEnum.forClassificatorId(recipient.getStatusId()).getClassificatorName());
        AadressType adr = new AadressType();
        adr.setRegnr(
            persistenceService.toDvkCapsuleAddressee(
                recipient.getOrganisation().getRegistrationCode(),
                recipient.getOrganisation().getSubSystem()));
        adr.setIsikukood(recipient.getPersonalcode());
        adr.setAsutuseNimi(recipient.getOrganisation().getName());
        edastus.setSaaja(adr);
        edastus.setSaadud(ConversionUtil.toGregorianCalendar(recipient.getSendingStart()));

        edastus.setMeetod("xtee");
        if (recipient.getSendingEnd() != null) {
          edastus.setLoetud(ConversionUtil.toGregorianCalendar(recipient.getSendingEnd()));
        }
        if (recipient.getFaultCode() != null) {
          ee.ria.dhx.server.types.ee.riik.schemas.dhl.Fault fault =
              new ee.ria.dhx.server.types.ee.riik.schemas.dhl.Fault();
          fault.setFaultactor(recipient.getFaultActor());
          fault.setFaultcode(recipient.getFaultCode());
          fault.setFaultdetail(recipient.getFaultDetail());
          fault.setFaultstring(recipient.getFaultString());
          edastus.setFault(fault);
        }
        edastus.setEdastatud(ConversionUtil.toGregorianCalendar(recipient.getSendingStart()));
        item.getEdastus().add(edastus);
        if (request.getKeha().isStaatuseAjalugu()) {
          for (StatusHistory recipientHistory : recipient.getStatusHistory()) {
            Status status = new Status();
            if (recipientHistory.getRecipientStatusId() != null) {
              status.setVastuvotjaStaatusId(
                  BigInteger.valueOf(recipientHistory.getRecipientStatusId()));
            }
            status.setStaatuseMuutmiseAeg(
                ConversionUtil.toGregorianCalendar(recipientHistory.getStatusChangeDate()));
            StatusEnum statusEnum =
                StatusEnum.forClassificatorId(recipientHistory.getStatusId());
            StatusType statusType = null;
            switch (statusEnum) {
              case RECEIVED:
                statusType = StatusType.SAADETUD;
                break;
              case FAILED:
                statusType = StatusType.KATKESTATUD;
                break;
              case IN_PROCESS:
                statusType = StatusType.SAATMISEL;
                break;
              default:
                throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Unknown status of.");
            }
            status.setStaatus(statusType);
            if (recipientHistory.getFaultCode() != null) {
              ee.ria.dhx.server.types.ee.riik.schemas.dhl.Fault fault =
                  new ee.ria.dhx.server.types.ee.riik.schemas.dhl.Fault();
              fault.setFaultactor(recipient.getFaultActor());
              fault.setFaultcode(recipient.getFaultCode());
              fault.setFaultdetail(recipient.getFaultDetail());
              fault.setFaultstring(recipient.getFaultString());
              status.setFault(fault);
            }
            status.setStaatuseAjaluguId(
                BigInteger.valueOf(recipientHistory.getStatusHistoryId()));
            StatusHistoryType.Status.Saaja saaja = new StatusHistoryType.Status.Saaja();
            saaja.setRegnr(persistenceService.toDvkCapsuleAddressee(
                recipient.getOrganisation().getRegistrationCode(),
                recipient.getOrganisation().getSubSystem()));
            saaja.setIsikukood(recipient.getPersonalcode());
            saaja.setAllyksuseLyhinimetus(recipient.getStructuralUnit());
            status.setSaaja(saaja);
            history.getStatus().add(status);
          }
        }
        item.setStaatuseAjalugu(history);
      }
      responseAtt.getItem().add(item);
    }
    DataHandler handler = convertationService.createDatahandlerFromObject(responseAtt);
    response.getKeha().setHref(handler);
    return response;
  }

  private void setGetSendingOptionsRequestBody(GetSendingOptions requestWrapper,
      MessageContext context, InternalXroadMember recipientMember) throws DhxException {
    GetSendingOptionsV2RequestType request = new GetSendingOptionsV2RequestType();
    if (requestWrapper.getAny() != null && requestWrapper.getAny().size() > 0
        && requestWrapper.getAny().get(0).getTagName().equals("keha")) {
      Node keha = requestWrapper.getAny().get(0);
      log.debug("Found keha element in request.");
      if (recipientMember.getServiceVersion().equals("v1")
          || recipientMember.getServiceVersion().equals("v2")) {
        log.debug("Dealing with request in body(v1, v2).");
        request = new GetSendingOptionsV2RequestType();
        InstitutionRefsArrayType institutions = new InstitutionRefsArrayType();
        request.setAsutused(institutions);
        NodeList list = keha.getOwnerDocument().getElementsByTagName("asutus");
        if (list != null) {
          for (int j = 0; j < list.getLength(); j++) {
            Node node = list.item(j);
            log.debug("asutus " + node.getTextContent());
            institutions.getAsutus().add(node.getTextContent());
          }
        }
      } else {
        for (int i = 0; i < keha.getAttributes().getLength(); i++) {
          Node att = keha.getAttributes().item(i);
          if (att.getLocalName().equals("href")) {
            String content = att.getTextContent();
            log.debug("Dealing with request in attachment defined in href(v3).");
            DataHandler attachmentHandler = WsUtil.extractAttachment(context, content);
            org.w3c.dom.Document xmlDoc = WsUtil.xmlDocumentFromStream(
                WsUtil.base64DecodeIfNeededAndUnzip(
                    attachmentHandler));
            InstitutionRefsArrayType institutions = new InstitutionRefsArrayType();
            request.setAsutused(institutions);
            NodeList list = xmlDoc.getElementsByTagName("asutus");
            if (list != null) {
              for (int j = 0; j < list.getLength(); j++) {
                Node node = list.item(j);
                log.debug("asutus " + node.getTextContent());
                institutions.getAsutus().add(node.getTextContent());
              }
            }
          }
          break;
        }
      }
    }
    requestWrapper.setKeha(request);
  }

  /**
   * Method returns list of organisations that are able to receive the documents using DHX protocol.
   * 
   * @param request SOAP request object
   * @param senderMember sender of the request(from SOAP header)
   * @param recipientMember recipient of the request(from SOAP header)
   * @param context Soap message context
   * @return SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public GetSendingOptionsResponse getSendingOptions(GetSendingOptions request,
      InternalXroadMember senderMember,
      InternalXroadMember recipientMember, MessageContext context) throws DhxException {
    if (request.getKeha() == null) {
      setGetSendingOptionsRequestBody(request, context, recipientMember);
    }
    GetSendingOptionsResponse response = new GetSendingOptionsResponse();
    ObjectFactory fact = new ObjectFactory();
    InstitutionArrayType institutions = fact.createInstitutionArrayType();
    List<Organisation> orgs = persistenceService.getAdresseeList();

    if (orgs != null && orgs.size() > 0) {
      for (Organisation org : orgs) {
        InstitutionType institution = new InstitutionType();
        if (org.getRepresentor() == null) {
          institution.setRegnr(persistenceService.toDvkCapsuleAddressee(org.getRegistrationCode(),
              org.getSubSystem()));

          if (!StringUtil.isNullOrEmpty(org.getRealName())) {
            institution.setNimi(org.getRealName());
          } else {
            String subsystem = "";
            if (!StringUtil.isNullOrEmpty(org.getSubSystem()) 
                && !soapConfig.getDhxSubsystemPrefix().equals(org.getSubSystem())) {
              subsystem = "(" 
                + org.getSubSystem() + ")";
            }
            institution.setNimi(org.getName() + subsystem);
          }


        } else {
          Date curDate = new Date(); // skip outdated
          if (org.getRepresenteeStart().getTime() > curDate.getTime()
              || (org.getRepresenteeEnd() != null
                  && org.getRepresenteeEnd().getTime() < curDate.getTime())) {
            continue;
          }
          institution.setRegnr(
              persistenceService.toDvkCapsuleAddressee(org.getRegistrationCode(),
                  org.getSubSystem()));
          if (!StringUtil.isNullOrEmpty(org.getRealName())) {
            institution.setNimi(org.getRealName());
          } else {
            String subsystem = "";
            if (!StringUtil.isNullOrEmpty(org.getSubSystem()) 
                && !soapConfig.getDhxSubsystemPrefix().equals(org.getSubSystem())) {
              subsystem = "(" 
                + org.getSubSystem() + ")";
            }
            institution.setNimi(org.getName() + subsystem);
          }
        }
        SendingOptionArrayType sendingOptions = new SendingOptionArrayType();
        sendingOptions.getSaatmisviis().add("dhl");
        institution.setSaatmine(sendingOptions);
        if (request.getKeha().getAsutused() != null
            && request.getKeha().getAsutused().getAsutus() != null
            && request.getKeha().getAsutused().getAsutus().size() > 0) {
          if (request.getKeha().getAsutused().getAsutus().contains(institution.getRegnr())) {
            institutions.getAsutus().add(institution);
          }
        } else {
          institutions.getAsutus().add(institution);
        }
      }
    }

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(false);
      DocumentBuilder db = dbf.newDocumentBuilder();
      org.w3c.dom.Document document = db.newDocument();
      
      if (recipientMember.getServiceVersion().equals("v1")
          || recipientMember.getServiceVersion().equals("v2")) {
      
        dhxMarshallerService.getMarshaller().marshal(institutions, document);
        List<Element> eles = new ArrayList<Element>();
        eles.add(document.getDocumentElement());
        response.setAny(eles);
      } else {

        GetSendingOptionV3ResponseBody keha = fact.createGetSendingOptionV3ResponseBody();
        
        keha.setAsutused(institutions);
        
        DataHandler handler = convertationService.createDatahandlerFromObject(keha);
        String contentId = WsUtil.addAttachment(context, handler);
        dhxMarshallerService.getMarshaller().marshal(fact.createGetSendingOptionV3ResponseBody(), document);
        document.getDocumentElement().setAttribute("href", contentId);
      }
      
      List<Element> eles = new ArrayList<Element>();
      eles.add(document.getDocumentElement());
      response.setAny(eles);
      
    } catch (JAXBException | ParserConfigurationException ex) {
      throw new DhxException("Error occured while marshalling response.", ex);
    }
    return response;
  }

  /**
   * Method deletes documents or content of the documents older than configured lifetime of the
   * received and failed documents. Documents with status INPROCESS are not deleted.
   * 
   * @param deleteWholeDocument delete whole document from database or only content.
   * @throws DhxException thrown when error occurs
   */
  @Loggable
  public void deleteOldDocuments(Boolean deleteWholeDocument) throws DhxException {
    Calendar receivedDocumentDate = Calendar.getInstance();
    receivedDocumentDate.add(Calendar.DAY_OF_YEAR, -receivedDocumentLifetime);

    Calendar failedDocumentDate = Calendar.getInstance();
    failedDocumentDate.add(Calendar.DAY_OF_YEAR, -failedDocumentLifetime);
    log.debug("Deleting receivedDocumentDate: " + receivedDocumentDate.getTime());
    List<Document> documents =
        documentRepository.findByDateCreatedLessThanAndTransportsStatusId(
            receivedDocumentDate.getTime(), StatusEnum.RECEIVED.getClassificatorId());
    if (documents.size() > 0) {
      log.debug("Found received documents to delete: " + documents.size());
    }
    // delete file withcontent
    for (Document doc : documents) {
      if (doc.getContent() != null) {
        dhxServerConfig.getDocumentFile(doc.getContent()).delete();
      }
    }
    if (deleteWholeDocument) {
      documentRepository.delete(documents);
    } else {
      for (Document doc : documents) {
        doc.setContent(null);
      }
      documentRepository.save(documents);
    }

    documents = documentRepository.findByDateCreatedLessThanAndTransportsStatusId(
        failedDocumentDate.getTime(), StatusEnum.FAILED.getClassificatorId());
    if (documents.size() > 0) {
      log.debug("Found failed documents to delete: " + documents.size());
    }
    for (Document doc : documents) {
      if (doc.getContent() != null) {
        dhxServerConfig.getDocumentFile(doc.getContent()).delete();
      }
    }
    if (deleteWholeDocument) {
      documentRepository.delete(documents);
    } else {
      for (Document doc : documents) {
        doc.setContent(null);
      }
      documentRepository.save(documents);
    }

  }

}

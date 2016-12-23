package ee.ria.dhx.server.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
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
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionType;
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
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;

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
  

  @Value("${dhx.server.received-document-lifetime}")
  @Setter
  Integer receivedDocumentLifetime;

  @Value("${dhx.server.failed-document-lifetime}")
  @Setter
  Integer failedDocumentLifetime;

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
    List<Recipient> recipients = recipientRepository
        .findByStatusIdAndTransportDokumentOutgoingDocumentAndDhxInternalConsignmentIdNull(
            statusId, true);
    for (Recipient recipient : recipients) {
      try {
        Document document = recipient.getTransport().getDokument();
        Object container = capsuleService.getContainerFromDocument(document);
        capsuleService.formatCapsuleRecipientAndSender(container,
            recipient.getTransport().getSenders().get(0).getOrganisation(),
            recipient.getOrganisation(),
            true);
        File containerFile = dhxMarshallerService.marshall(container);
        if (recipient.getTransport().getSenders() == null
            || recipient.getTransport().getSenders().size() > 1) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "No sender is related to document or more than one sender is related!");
        }
        Organisation sendeOrg = recipient.getTransport().getSenders().get(0).getOrganisation();
        InternalXroadMember senderMember = null;
        try {
          senderMember = addressService.getClientForMemberCode(sendeOrg.getRegistrationCode(),
              sendeOrg.getSubSystem());
        } catch (DhxException ex) {
          log.debug(
              "Erro occured while searching org. ignoring error and continue!" + ex.getMessage(),
              ex);
        }
        Organisation recipientOrg = recipient.getOrganisation();
        InternalXroadMember recipientMember = addressService
            .getClientForMemberCode(recipientOrg.getRegistrationCode(),
                recipientOrg.getSubSystem());
        log.debug("Found recipient member: " + recipientMember.toString());
        OutgoingDhxPackage dhxPackage = null;
        // if sender org is null, then try sending with sender from
        // config
        if (senderMember != null) {
          dhxPackage = dhxPackageProviderService.getOutgoingPackage(containerFile,
              recipient.getRecipientId().toString(), recipientMember, senderMember);
        } else {
          dhxPackage = dhxPackageProviderService.getOutgoingPackage(containerFile,
              recipient.getRecipientId().toString(), recipientMember);
        }
        recipient.setDhxInternalConsignmentId(recipient.getRecipientId().toString());
        recipient.setSendingStart(new Timestamp((new Date()).getTime()));
        asyncDhxPackageService.sendPackage(dhxPackage);
      } catch (DhxException ex) {
        log.error("Error occured while sending document! " + ex.getMessage(), ex);
        Integer failedStatusId = StatusEnum.FAILED.getClassificatorId();
        recipient.setStatusId(failedStatusId);
        recipient.setFaultString(ex.getMessage());
        recipient.setFaultCode(ex.getExceptionCode().toString());
        recipient.setRecipientStatusId(RecipientStatusEnum.REJECTED.getClassificatorId());
      } finally {
        recipientRepository.save(recipient);
      }
    }
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
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(sender.getMemberCode(),
            sender.getSubsystemCode());
    Folder folder = null;
    if (request.getKeha().getKaust() != null) {
      folder = folderRepository.findByName(request.getKeha().getKaust());
    }
    Integer inprocessStatusId = StatusEnum.IN_PROCESS.getClassificatorId();
    List<Document> docs = null;
    if (folder == null && request.getKeha().getKaust() == null) {
      log.debug(
          "searching by recipients organisation and status " + senderOrg.getOrganisationId());
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
    ReceiveDocumentsResponse resp = fact.createReceiveDocumentsResponse();
    Base64BinaryType att = fact.createBase64BinaryType();
    List<Object> containers = new ArrayList<Object>();
    for (Document doc : docs) {
      Object container = capsuleService.getContainerFromDocument(doc);
      capsuleService.formatCapsuleRecipientAndSender(container,
          doc.getTransports().get(0).getSenders().get(0).getOrganisation(), senderOrg, false);
      containers.add(container);
    }
    DataHandler handler = convertationService.createDatahandlerFromList(containers);
    att.setHref(handler);
    resp.setKeha(att);
    return resp;
  }

  /**
   * Method marks documents with status provided in request. Documents which will be marked are
   * parameters provided in request.
   * 
   * @param request - SOAP request object
   * @param senderMember - sender of the request(from SOAP header)
   * @param recipientMember - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public MarkDocumentsReceivedResponse markDocumentReceived(
      MarkDocumentsReceivedV3RequestType request,
      InternalXroadMember senderMember, InternalXroadMember recipientMember) throws DhxException {
    if (request.getDokumendid() == null || request.getDokumendid().size() == 0) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "No documents to mark received is provided in request.");
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
    }
    Boolean allSent = true;
    Boolean allFailed = true;
    Boolean found = false;
    for (TagasisideType status : request.getDokumendid()) {
      Document doc = documentRepository.findOne(status.getDhlId().longValue());
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
        doc.getTransports().get(0).setStatusId(successStatusId);
        documentRepository.save(doc);
      }
      if (allFailed && !doc.getTransports().get(0).getStatusId()
          .equals(StatusEnum.FAILED.getClassificatorId())) {
        doc.getTransports().get(0).setStatusId(failedStatusId);
        documentRepository.save(doc);
      }
    }
    ObjectFactory factory = new ObjectFactory();
    MarkDocumentsReceivedResponse response = factory.createMarkDocumentsReceivedResponse();
    response.setKeha("OK");
    return response;
  }

  /**
   * Method returns statuses of the documents. Documents are found by the parameters provided in
   * request.
   * 
   * @param request - SOAP request object
   * @param senderMember - sender of the request(from SOAP header)
   * @param recipientMember - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public GetSendStatusResponse getSendStatus(GetSendStatus request,
      InternalXroadMember senderMember,
      InternalXroadMember recipientMember) throws DhxException {
    try {
      org.w3c.dom.Document xmlDoc = WsUtil.xmlDocumentFromStream(
          WsUtil.base64decodeAndUnzip(
              request.getKeha().getDokumendid().getHref().getInputStream()));
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
      list = xmlDoc.getElementsByTagName("dokument_guid");
      if (list.getLength() > 0) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Getting send status by dokument_guid is not supported");
      }
      List<Document> documents = documentRepository.findByDocumentIdIn(dhlIds);
      ObjectFactory factory = new ObjectFactory();
      GetSendStatusResponse response = factory.createGetSendStatusResponse();
      response.setKeha(factory.createBase64BinaryType());
      GetSendStatusV2ResponseTypeUnencoded responseAtt =
          factory.createGetSendStatusV2ResponseTypeUnencoded();
      for (Document doc : documents) {
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
          // adr.setAllyksuseKood(recipient.getStructuralUnit());
          adr.setAsutuseNimi(recipient.getOrganisation().getName());
          edastus.setSaaja(adr);
          edastus.setSaadud(ConversionUtil.toGregorianCalendar(recipient.getSendingStart()));

          // edastus.setMetaxml(recipient.getMetaxml());
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
              // status.setMetaxml(value);
              history.getStatus().add(status);
            }
          }
          item.setStaatuseAjalugu(history);
        }
        responseAtt.getItem().add(item);
      }
      DataHandler handler = convertationService.createDatahandlerFromList(responseAtt.getItem());
      response.getKeha().setHref(handler);
      return response;
    } catch (IOException ex) {
      throw new DhxException("Error occured while getting attachment. " + ex.getMessage(), ex);
    }
  }

  /**
   * Method returns list of organisations that are able to receive the documents using DHX protocol.
   * 
   * @param request - SOAP request object
   * @param senderMember - sender of the request(from SOAP header)
   * @param recipientMember - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public GetSendingOptionsResponse getSendingOptions(GetSendingOptions request,
      InternalXroadMember senderMember,
      InternalXroadMember recipientMember) throws DhxException {
    // request.getKeha().getAsutused().getAsutus()
    GetSendingOptionsResponse response = new GetSendingOptionsResponse();
    response.setKeha(new Base64BinaryType());
    InstitutionArrayType institutions = new InstitutionArrayType();
    for (InternalXroadMember org : addressService.getAdresseeList()) {
      InstitutionType institution = new InstitutionType();
      if (org.getRepresentee() == null) {
        institution.setRegnr(
            persistenceService.toDvkCapsuleAddressee(org.getMemberCode(),
                org.getSubsystemCode()));
        institution.setNimi(org.getName());

      } else {
        Date curDate = new Date();
        // skip outdated
        if (org.getRepresentee().getStartDate().getTime() > curDate.getTime()
            || (org.getRepresentee().getEndDate() != null
                && org.getRepresentee().getEndDate().getTime() < curDate.getTime())) {
          continue;
        }
        institution.setRegnr(
            persistenceService.toDvkCapsuleAddressee(org.getRepresentee().getRepresenteeCode(),
                org.getRepresentee().getRepresenteeSystem()));
        institution.setNimi(org.getRepresentee().getRepresenteeName());
      }
      SendingOptionArrayType sendingOptions = new SendingOptionArrayType();
      sendingOptions.getSaatmisviis().add("dhl");
      institution.setSaatmine(sendingOptions);
      institutions.getAsutus().add(institution);
    }
    DataHandler handler = convertationService.createDatahandlerFromObject(institutions);
    response.getKeha().setHref(handler);
    return response;
  }
  
  /**
   * Method deletes documents or content of the documents older than configured lifetime of the
   * received and failed documents. In sending process documents are not deleted.
   * 
   * @param deleteWholeDocument delete whole document from database or only content.
   */
  @Loggable
  public void deleteOldDocuments(Boolean deleteWholeDocument) {
    Calendar receivedDocumentDate = Calendar.getInstance();
    receivedDocumentDate.add(Calendar.DAY_OF_YEAR, -receivedDocumentLifetime);
    
    Calendar failedDocumentDate = Calendar.getInstance();
    failedDocumentDate.add(Calendar.DAY_OF_YEAR, -failedDocumentLifetime);
    log.debug("receivedDocumentDate: " + receivedDocumentDate.getTime());
    List<Document> documents =
        documentRepository.findByDateCreatedLessThanAndTransportsStatusId(
          receivedDocumentDate.getTime(), StatusEnum.RECEIVED.getClassificatorId());
    if(documents.size()>0) {
      log.debug("Found received documents to delete: " + documents.size());
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
    if(documents.size()>0) {
      log.debug("Found failed documents to delete: " + documents.size());
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

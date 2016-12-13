package ee.ria.dhx.server.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.StatusHistory;
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
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
  DhxMarshallerService dhxMarshallerService;

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


  @Autowired
  ConvertationService convertationService;

  @Autowired
  CapsuleService capsuleService;

  @Autowired
  PersistenceService persistenceService;


  /**
   * Method processes sendDocuments request and saves the document into the database for further
   * sending to DHX service of the recipient.
   * 
   * @param documents - SOAP request object
   * @param sender - sender of the request(from SOAP header)
   * @param recipient - recipient of the request(from SOAP header)
   * @return - SOAP response object
   * @throws DhxException
   */
  @Loggable
  public SendDocumentsResponse sendDocuments(SendDocuments documents,
      InternalXroadMember sender, InternalXroadMember recipient) throws DhxException {
    // for now support only v21
    CapsuleVersionEnum version = CapsuleVersionEnum.V21;
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(sender, recipient, documents
            .getKeha().getDokumendid().getHref(), documents.getKeha().getKaust(), version);
    documentRepository.save(document);
    ObjectFactory fact = new ObjectFactory();
    SendDocumentsResponse response = fact.createSendDocumentsResponse();
    response.setKeha(fact.createBase64BinaryType());
    SendDocumentsV4ResponseTypeUnencoded.Keha attachmentObj =
        fact.createSendDocumentsV4ResponseTypeUnencodedKeha();
    attachmentObj.getDhlId().add(document.getDocumentId().toString());
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
    List<Recipient> recipients =
        recipientRepository
            .findByStatusIdAndTransportDokumentOutgoingDocumentAndDhxInternalConsignmentIdNull(
                statusId, true);
    for (Recipient recipient : recipients) {
      try {
        Document document = recipient.getTransport().getDokument();
        Object container = capsuleService.getContainerFromDocument(document);
        File containerFile = dhxMarshallerService.marshall(container);
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
              dhxPackageProviderService.getOutgoingPackage(containerFile, recipient
                  .getRecipientId().toString(), recipientMember, senderMember);
        }
        else {
          dhxPackage =
              dhxPackageProviderService.getOutgoingPackage(containerFile, recipient
                  .getRecipientId().toString(), recipientMember);
        }
        recipient.setDhxInternalConsignmentId(recipient.getRecipientId().toString());
        recipient.setSendingStart(new Timestamp((new Date()).getTime()));
        asyncDhxPackageService.sendPackage(dhxPackage);
      } catch (DhxException ex) {
        log.error("Error occured while sending document! " + ex.getMessage(), ex);
        Integer failedStatusId = StatusEnum.FAILED.getClassificatorId();
        recipient.setStatusId(failedStatusId);
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
   * @throws DhxException
   */
  @Loggable
  public ReceiveDocumentsResponse receiveDocuments(ReceiveDocuments request,
      InternalXroadMember sender, InternalXroadMember recipient) throws DhxException {

    // TODO: isikukood, yksus, arv
    Iterable<Document> doccs = documentRepository.findAll();
    for (Document docc : doccs) {
      log.debug("org" + docc.getDocumentId());
    }
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(sender.getMemberCode(),
            sender.getSubsystemCode());
    Folder folder =
        persistenceService.getFolderByNameOrDefaultFolder(request.getKeha().getKaust());
    Integer inprocessStatusId = StatusEnum.IN_PROCESS.getClassificatorId();
    List<Document> docs =
        documentRepository
            .findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
                senderOrg,
                inprocessStatusId, folder);
    ObjectFactory fact = new ObjectFactory();
    ReceiveDocumentsResponse resp = fact.createReceiveDocumentsResponse();
    Base64BinaryType att = fact.createBase64BinaryType();
    List<Object> containers = new ArrayList<Object>();
    for (Document doc : docs) {
      Object container = capsuleService.getContainerFromDocument(doc);
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
   * @throws DhxException
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
    /*
     * Classificator inprocessStatus =
     * classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
     */
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
    Boolean found = false;
    for (TagasisideType status : request.getDokumendid()) {
      Document doc = documentRepository.findByDocumentId(status.getDhlId().intValue());
      if (doc == null) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Document is not found.");
      }
      if (folder != null && !doc.getFolder().getFolderId().equals(folder.getFolderId())) {
        continue;
      }
      for (Recipient recipient : doc.getTransports().get(0).getRecipients()) {
        if (recipient.getOrganisation().getOrganisationId().equals(senderOrg.getOrganisationId())) {
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
            Timestamp date =
                new Timestamp(status.getStaatuseMuutmiseAeg().toGregorianCalendar().getTime()
                    .getTime());
            recipient.setStatusChangeDate(date);
          } else {
            recipient.setStatusChangeDate(new Timestamp(new Date().getTime()));
          }
          recipient.setSendingEnd(new Timestamp(new Date().getTime()));
          recipientRepository.save(recipient);
        }
        if (!recipient.getStatusId().equals(StatusEnum.RECEIVED.getClassificatorId())) {
          allSent = false;
        }
      }
      if (!found) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "That document is not sent to recipient organisation. dhlId:"
                + status
                    .getDhlId().longValue() + " organisation:" + senderOrg.getRegistrationCode());
      }
      if (allSent
          && !doc.getTransports().get(0).getStatusId()
              .equals(StatusEnum.RECEIVED.getClassificatorId())) {
        doc.getTransports().get(0).setStatusId(successStatusId);
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
   * @throws DhxException
   */
  @Loggable
  public GetSendStatusResponse getSendStatus(GetSendStatus request,
      InternalXroadMember senderMember, InternalXroadMember recipientMember) throws DhxException {
    try {
      org.w3c.dom.Document xmlDoc =
          WsUtil.xmlDocumentFromStream(WsUtil.base64decodeAndUnzip(request
              .getKeha()
              .getDokumendid().getHref()
              .getInputStream()));
      List<Integer> dhlIds = new ArrayList<Integer>();
      NodeList list = xmlDoc.getElementsByTagName("dhl_id");
      for (int i = 0; i < list.getLength(); i++) {
        Node node = list.item(i);
        log.debug("dhl id " + node.getTextContent());
        dhlIds.add(Integer.valueOf(node.getTextContent()));
      }
      // TODO: check is sender org is the same as the documetns org
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
        GetSendStatusV2ResponseTypeUnencoded.Item item =
            factory.createGetSendStatusV2ResponseTypeUnencodedItem();
        StatusHistoryType history = null;
        if (request.getKeha().isStaatuseAjalugu()) {
          history = new StatusHistoryType();
        }
        item.setDhlId(doc.getDocumentId().toString());
        item.setOlek(StatusEnum.forClassificatorId(doc.getTransports().get(0).getStatusId())
            .getClassificatorName());
        for (Recipient recipient : doc.getTransports().get(0).getRecipients()) {
          Edastus edastus = new Edastus();
          if (recipient.getRecipientStatusId() != null) {
            edastus.setVastuvotjaStaatusId(BigInteger.valueOf(recipient.getRecipientStatusId()));
          }
          edastus.setStaatus(StatusEnum.forClassificatorId(recipient.getStatusId())
              .getClassificatorName());
          AadressType adr = new AadressType();
          adr.setRegnr(recipient.getOrganisation().getRegistrationCode());
          adr.setIsikukood(recipient.getPersonalcode());
          // adr.setAllyksuseKood(recipient.getStruCturalUnit());
          adr.setAsutuseNimi(recipient.getOrganisation().getName());
          edastus.setSaaja(adr);
          edastus.setSaadud(ConversionUtil.toGregorianCalendar(recipient
              .getSendingStart()));

          // edastus.setMetaxml(recipient.getMetaxml());
          // edastus.setMeetod();
          if (recipient.getSendingEnd() != null) {
            edastus.setLoetud(ConversionUtil.toGregorianCalendar(recipient
                .getSendingEnd()));
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
          edastus.setEdastatud(ConversionUtil.toGregorianCalendar(recipient
              .getSendingStart()));
          item.getEdastus().add(edastus);
          if (request.getKeha().isStaatuseAjalugu()) {
            for (StatusHistory recipientHistory : recipient.getStatusHistory()) {
              Status status = new Status();
              if (recipientHistory.getRecipientStatusId() != null) {
                status.setVastuvotjaStaatusId(BigInteger.valueOf(recipientHistory
                    .getRecipientStatusId()));
              }
              status.setStaatuseMuutmiseAeg(ConversionUtil.toGregorianCalendar(recipientHistory
                  .getStatusChangeDate()));
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
              status.setStaatuseAjaluguId(BigInteger.valueOf(recipientHistory
                  .getStatusHistoryId()));
              StatusHistoryType.Status.Saaja saaja = new StatusHistoryType.Status.Saaja();
              saaja.setRegnr(recipient.getOrganisation().getRegistrationCode());
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
   * @throws DhxException
   */
  @Loggable
  public GetSendingOptionsResponse getSendingOptions(GetSendingOptions request,
      InternalXroadMember senderMember, InternalXroadMember recipientMember) throws DhxException {
    // request.getKeha().getAsutused().getAsutus()
    GetSendingOptionsResponse response = new GetSendingOptionsResponse();
    response.setKeha(new InstitutionArrayType());

    for (InternalXroadMember org : addressService.getAdresseeList()) {
      InstitutionType institution = new InstitutionType();
      institution.setNimi(org.getName());
      institution.setRegnr(org.getMemberCode());
      SendingOptionArrayType sendingOptions = new SendingOptionArrayType();
      sendingOptions.getSaatmisviis().add("dhl");
      institution.setSaatmine(sendingOptions);
      // institutions.
      response.getKeha().getAsutus().add(institution);
    }
    return response;
  }



}

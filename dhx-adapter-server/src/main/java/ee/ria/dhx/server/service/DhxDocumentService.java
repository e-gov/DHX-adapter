package ee.ria.dhx.server.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Document;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.entity.Recipient;
import ee.ria.dhx.server.repository.ClassificatorRepository;
import ee.ria.dhx.server.repository.DocumentRepository;
import ee.ria.dhx.server.repository.FolderRepository;
import ee.ria.dhx.server.repository.OrganisationRepository;
import ee.ria.dhx.server.repository.RecipientRepository;
import ee.ria.dhx.server.service.util.AttachmentUtil;
import ee.ria.dhx.server.service.util.StatusEnum;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedV3RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ObjectFactory;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.TagasisideType;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.CapsuleConfig;
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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


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


  @Autowired
  ConvertationService convertationService;


  public SendDocumentsResponse sendDocuments(SendDocuments documents,
      InternalXroadMember sender, InternalXroadMember recipient) throws DhxException {
    Document document =
        convertationService.getDocumentFromOutgoingContainer(sender, recipient, documents
            .getKeha().getDokumendid().getHref(), documents.getKeha().getKaust());
    documentRepository.save(document);
    ObjectFactory fact = new ObjectFactory();
    SendDocumentsResponse response = fact.createSendDocumentsResponse();
    response.setKeha(fact.createBase64BinaryType());
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    // SendDocumentsV4ResponseTypeUnencoded attachmentObj =
    // fact.createSendDocumentsV4ResponseTypeUnencoded();
    SendDocumentsV4ResponseTypeUnencoded.Keha attachmentObj =
        fact.createSendDocumentsV4ResponseTypeUnencodedKeha();
    attachmentObj.getDhlId().add(document.getDocumentId().toString());
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = AttachmentUtil.getBase64EncodeStream(fos);
      zippedStream = AttachmentUtil.getGZipCompressStream(base64Stream);
      dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(attachmentObj, zippedStream);
      zippedStream.finish();
      base64Stream.flush();
      fos.flush();
      DataSource datasource = new FileDataSource(file);
      DataHandler handler = new DataHandler(datasource);
      response.getKeha().setHref(handler);
      return response;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }
  }



  public void sendDocumentsToDhx() {
    Classificator status =
        classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
    List<Recipient> recipients =
        recipientRepository
            .findByStatusAndTransportDokumentOutgoingDocumentAndDhxInternalConsignmentIdNull(
                status, true);
    for (Recipient recipient : recipients) {
      // InputStream containerStream = null;
      try {
        Document document = recipient.getTransport().getDokument();
        DecContainer container = convertationService.getContainerFromDocument(document);
        File containerFile = dhxMarshallerService.marshall(container);
        /*
         * containerStream = new
         * ByteArrayInputStream(recipient.getTransport().getDokument().getContent()
         * .getBytes("UTF-8"));
         */
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
      } catch (DhxException /* | UnsupportedEncodingException */ex) {
        log.error("Error occured while sending document! " + ex.getMessage(), ex);
        Classificator classificator =
            classificatorRepository.findByName(StatusEnum.FAILED.getClassificatorName());
        recipient.setStatus(classificator);
      } finally {
        // FileUtil.safeCloseStream(containerStream);
        recipientRepository.save(recipient);
      }
    }
  }

  public ReceiveDocumentsResponse receiveDocuments(ReceiveDocuments request,
      InternalXroadMember sender, InternalXroadMember recipient) throws DhxException {
    Iterable<Document> doccs = documentRepository.findAll();
    for (Document docc : doccs) {
      log.debug("org" + docc.getDocumentId());
    }
    Organisation senderOrg =
        organisationRepository.findByRegistrationCodeAndSubSystem(sender.getMemberCode(),
            sender.getSubsystemCode());
    Folder folder =
        convertationService.getFolderByNameOrDefaultFolder(request.getKeha().getKaust());
    Classificator inprocessStatus =
        classificatorRepository.findByName(StatusEnum.IN_PROCESS.getClassificatorName());
    List<Document> docs =
        documentRepository
            .findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusAndFolder(
                senderOrg,
                inprocessStatus, folder);
    ObjectFactory fact = new ObjectFactory();
    ReceiveDocumentsResponse resp = fact.createReceiveDocumentsResponse();
    Base64BinaryType att = fact.createBase64BinaryType();
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = AttachmentUtil.getBase64EncodeStream(fos);
      zippedStream = AttachmentUtil.getGZipCompressStream(base64Stream);
      for (Document doc : docs) {
        DecContainer container = convertationService.getContainerFromDocument(doc);

        dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(container, zippedStream);
      }
      zippedStream.finish();
      base64Stream.flush();
      fos.flush();
      DataSource datasource = new FileDataSource(file);
      DataHandler handler = new DataHandler(datasource);
      att.setHref(handler);
      resp.setKeha(att);
      return resp;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }

  }

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
    Classificator failedStatus =
        classificatorRepository.findByName(StatusEnum.FAILED.getClassificatorName());
    Classificator successStatus =
        classificatorRepository.findByName(StatusEnum.RECEIVED.getClassificatorName());
    Folder folder = null;
    if (request.getKaust() != null) {
      folder = folderRepository.findByName(request.getKaust());
    }
    if (senderOrg == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Senders organisation not found. organisation:" + senderMember.toString());
    }
    for (TagasisideType status : request.getDokumendid()) {
      Recipient recipient;
      if (folder == null) {
        recipient =
            recipientRepository.findByTransportDokumentDocumentId(status
                .getDhlId().intValue(), senderOrg);
      } else {
        recipient =
            recipientRepository
                .findByTransportDokumentDocumentIdAndTransportDokumentFolder(
                    status.getDhlId().intValue(), folder, senderOrg);
      }
      if (recipient == null) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "No document with id dhlId:"
            + status
                .getDhlId().longValue() + " is sent to organisation:" + senderOrg.getRegistrationCode());
      }
      if (!senderOrg.getOrganisationId().equals(recipient.getOrganisation().getOrganisationId())) {
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "That document is not sent to recipient organisation. dhlId:"
                + status
                    .getDhlId().longValue() + " organisation:" + senderOrg.getRegistrationCode());
      }
      if (status.getFault() != null) {
        recipient.setFaultActor(status.getFault().getFaultactor());
        recipient.setFaultCode(status.getFault().getFaultcode());
        recipient.setFaultDetail(status.getFault().getFaultdetail());
        recipient.setFaultString(status.getFault().getFaultstring());
        recipient.setStatus(failedStatus);
      } else {
        recipient.setStatus(successStatus);
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
      }
      recipientRepository.save(recipient);
      // TODO: set recipient status
    }
    ObjectFactory factory = new ObjectFactory();
    MarkDocumentsReceivedResponse response = factory.createMarkDocumentsReceivedResponse();
    response.setKeha("OK");
    return response;
  }

  public GetSendStatusResponse getSendStatus(GetSendStatus request,
      InternalXroadMember senderMember, InternalXroadMember recipientMember) throws DhxException {
    try {
     /* log.debug("got file " + AttachmentUtil.readInput(request.getKeha().getDokumendid().getHref()
        .getInputStream()));*/
      org.w3c.dom.Document doc = xmlDocumentFromStream(AttachmentUtil.base64decodeAndUnzip(request.getKeha().getDokumendid().getHref()
        .getInputStream()));
      List<Integer> dhlIds = new ArrayList<Integer>();
      NodeList list = doc.getElementsByTagName("dhl_id");
      for(int i = 0; i<list.getLength(); i++) {
        Node node = list.item(i);
        log.debug("dhl id " + node.getTextContent());
        dhlIds.add(Integer.valueOf(node.getTextContent()));
      }
      if(dhlIds.size()==0){
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "No dhl ids are provided to get status for.");
      }
      list = doc.getElementsByTagName("dokument_guid");
      if(list.getLength()>0){
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Getting send statud by dokument_guid is not supported");
      }
      List<Document> documents = documentRepository.findByDocumentIdIn(dhlIds);
      ObjectFactory factory = new ObjectFactory();
      GetSendStatusV2ResponseTypeUnencoded response = factory.createGetSendStatusV2ResponseTypeUnencoded();
      for(Document doc : documents) {
        
      }
      response.getItem().add(e)
      
     /* Object obj =
          dhxMarshallerService.unmarshall(AttachmentUtil.base64decodeAndUnzip(request.getKeha().getDokumendid().getHref()
              .getInputStream()));*/
      return null;
    } catch (IOException ex) {
      throw new DhxException("Error occured while getting attachment. " + ex.getMessage(), ex);
    }
  }
  
  public static org.w3c.dom.Document xmlDocumentFromStream(InputStream objectStream) throws DhxException{
    try {
        DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
        xmlFact.setValidating(false);
        xmlFact.setNamespaceAware(false);
        xmlFact.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = xmlFact.newDocumentBuilder();
        FileInputStream inStream = null;
        InputStreamReader inReader = null;
        org.w3c.dom.Document result = null;
        try {
            //inStream = new FileInputStream(filePath);
            inReader = new InputStreamReader(objectStream, "UTF-8");
            InputSource src = new InputSource(inReader);
            result = builder.parse(src);
        } finally {
            FileUtil.safeCloseReader(inReader);
            FileUtil.safeCloseStream(inStream);
            inReader = null;
            inStream = null;
        }
        return result;
    } catch (IOException | SAXException | ParserConfigurationException ex) {
       throw new DhxException("Error occured while parsing XML. " + ex.getMessage(), ex);
    }
}

}

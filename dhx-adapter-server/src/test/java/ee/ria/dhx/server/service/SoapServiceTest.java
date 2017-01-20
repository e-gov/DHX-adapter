package ee.ria.dhx.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.Sender;
import ee.ria.dhx.server.persistence.entity.StatusHistory;
import ee.ria.dhx.server.persistence.entity.Transport;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.DocumentRepository;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.persistence.repository.RecipientRepository;
import ee.ria.dhx.server.persistence.service.CapsuleService;
import ee.ria.dhx.server.persistence.service.PersistenceService;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Dokumendid;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Fault;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsV2RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedV3RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.StatusType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.TagasisideType;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.AsyncDhxPackageService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import javax.xml.datatype.XMLGregorianCalendar;

public class SoapServiceTest {

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Mock
  FolderRepository folderRepository;

  @Mock
  OrganisationRepository organisationRepository;

  @Mock
  AddressService addressService;

  @Mock
  DocumentRepository documentRepository;

  @Mock
  RecipientRepository recipientRepository;

  @Mock
  AsyncDhxPackageService asyncDhxPackageService;

  @Mock
  DhxPackageProviderService dhxPackageProviderService;

  @Mock
  ConvertationService convertationService;

  @Mock
  CapsuleService capsuleService;

  @Mock
  PersistenceService persistenceService;

  @Mock
  SoapConfig config;

  SoapService soapService;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  private InternalXroadMember getMember(String memberCode, DhxRepresentee representee) {
    return new InternalXroadMember("ee-dev", "GOV", memberCode, "DHX", "Name1", representee);
  }

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    soapService = new SoapService();
    soapService.setAddressService(addressService);
    soapService.setAsyncDhxPackageService(asyncDhxPackageService);
    soapService.setCapsuleService(capsuleService);
    soapService.setConvertationService(convertationService);
    soapService.setDhxMarshallerService(dhxMarshallerService);
    soapService.setDhxPackageProviderService(dhxPackageProviderService);
    soapService.setDocumentRepository(documentRepository);
    soapService.setFolderRepository(folderRepository);
    soapService.setOrganisationRepository(organisationRepository);
    soapService.setPersistenceService(persistenceService);
    soapService.setRecipientRepository(recipientRepository);
    soapService.setSoapConfig(config);
    soapService.setResendTimeout(30);
    when(config.getDhxSubsystemPrefix()).thenReturn("DXH");
  }

  @Test
  public void sendDocuments() throws DhxException {
    DataHandler handler = Mockito.mock(DataHandler.class);
    DataHandler handlerAttachment = Mockito.mock(DataHandler.class);
    SendDocuments request = new SendDocuments();
    request.setKeha(new SendDocumentsV4RequestType());
    request.getKeha().setKaust("kaust");
    request.getKeha().setDokumendid(new Base64BinaryType());
    request.getKeha().getDokumendid().setHref(handler);
    InternalXroadMember sender = getMember("sender", null);
    InternalXroadMember recipient = getMember("recipient", null);
    Document doc = new Document();
    doc.setDocumentId(10L);
    List<Object> containers = new ArrayList<Object>();
    DecContainer container = new DecContainer();
    containers.add(container);
    when(capsuleService.getContainersList(handler, CapsuleVersionEnum.V21))
        .thenReturn(containers);
    when(capsuleService.getDocumentFromOutgoingContainer(sender, recipient, container, "kaust",
        CapsuleVersionEnum.V21)).thenReturn(doc);
    when(convertationService
        .createDatahandlerFromObject(any(SendDocumentsV4ResponseTypeUnencoded.Keha.class)))
            .thenReturn(handlerAttachment);
    SendDocumentsResponse response = soapService.sendDocuments(request, sender, recipient);
    verify(capsuleService, times(1)).getDocumentFromOutgoingContainer(sender, recipient,
        container, "kaust",
        CapsuleVersionEnum.V21);
    verify(documentRepository, times(1)).save(doc);
    assertEquals(handlerAttachment, response.getKeha().getHref());
  }

  @Test
  public void sendDocumentsToDhxNoSenderFound() throws DhxException {
    List<Recipient> recipients = new ArrayList<>(); // org, sender
    Organisation senderOrg = new Organisation();
    Sender sender = new Sender();
    sender.setOrganisation(senderOrg);
    senderOrg.setRegistrationCode("senderCode");
    senderOrg.setSubSystem("senderSystem");
    Organisation recipientOrg = new Organisation();
    recipientOrg.setRegistrationCode("recipientCode");
    recipientOrg.setSubSystem("recipientSystem");
    Recipient recipient = new Recipient();
    recipient.setRecipientId(2L);
    recipient.setTransport(new Transport());
    recipient.getTransport().addSender(sender);
    recipient.setOrganisation(recipientOrg);
    Document doc = new Document();
    recipient.getTransport().setDokument(doc);
    recipient.setRecipientId(12L);
    recipients.add(recipient);
    when(recipientRepository.findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNull(
        StatusEnum.IN_PROCESS.getClassificatorId(), true)).thenReturn(recipients);
    // when(recipientRepository.findByRecipientId(any(Long.class))).thenReturn(recipient);
    DecContainer container = new DecContainer();
    when(capsuleService.getContainerFromDocument(doc)).thenReturn(container);
    File containerFile = new File("");
    when(dhxMarshallerService.marshall(container)).thenReturn(containerFile);
    when(addressService.getClientForMemberCode(senderOrg.getRegistrationCode(),
        senderOrg.getSubSystem()))
            .thenReturn(null);
    InternalXroadMember recipientMember = getMember("recipientCode", null);
    when(addressService.getClientForMemberCode(recipientOrg.getRegistrationCode(),
        recipientOrg.getSubSystem()))
            .thenReturn(recipientMember);
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null, null, null);
    when(dhxPackageProviderService.getOutgoingPackage(containerFile, "12", recipientMember))
        .thenReturn(pckg);
    soapService.sendDocumentsToDhx();
    verify(dhxPackageProviderService, times(1)).getOutgoingPackage(containerFile, "12",
        recipientMember);
    verify(asyncDhxPackageService, times(1)).sendPackage(pckg);
  }

  @Test
  public void sendDocumentsToDhxSenderFound() throws DhxException {
    List<Recipient> recipients = new ArrayList<>();
    Organisation senderOrg = new Organisation();
    Sender sender = new Sender();
    sender.setOrganisation(senderOrg);
    senderOrg.setRegistrationCode("senderCode");
    senderOrg.setSubSystem("senderSystem");
    Organisation recipientOrg = new Organisation();
    recipientOrg.setRegistrationCode("recipientCode");
    recipientOrg.setSubSystem("recipientSystem");
    Recipient recipient = new Recipient();
    recipient.setRecipientId(2L);
    recipient.setTransport(new Transport());
    recipient.getTransport().addSender(sender);
    recipient.setOrganisation(recipientOrg);
    Document doc = new Document();
    recipient.getTransport().setDokument(doc);
    recipient.setRecipientId(12L);
    recipients.add(recipient);
    when(recipientRepository.findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNull(
        StatusEnum.IN_PROCESS.getClassificatorId(), true)).thenReturn(recipients);
    // when(recipientRepository.findByRecipientId(any(Long.class))).thenReturn(recipient);
    DecContainer container = new DecContainer();
    when(capsuleService.getContainerFromDocument(doc)).thenReturn(container);
    File containerFile = new File("");
    when(dhxMarshallerService.marshall(container)).thenReturn(containerFile);
    InternalXroadMember senderMember = getMember("senderCode", null);
    when(addressService.getClientForMemberCode(senderOrg.getRegistrationCode(),
        senderOrg.getSubSystem()))
            .thenReturn(senderMember);
    InternalXroadMember recipientMember = getMember("recipientCode", null);
    when(addressService.getClientForMemberCode(recipientOrg.getRegistrationCode(),
        recipientOrg.getSubSystem()))
            .thenReturn(recipientMember);
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null, null, null);
    when(dhxPackageProviderService.getOutgoingPackage(containerFile, "12", recipientMember,
        senderMember))
            .thenReturn(pckg);
    soapService.sendDocumentsToDhx();
    verify(dhxPackageProviderService, times(1)).getOutgoingPackage(containerFile, "12",
        recipientMember,
        senderMember);
    verify(asyncDhxPackageService, times(1)).sendPackage(pckg);
  }

  @Test
  public void receiveDocuments() throws DhxException {
    DataHandler handler = Mockito.mock(DataHandler.class);
    ReceiveDocuments request = new ReceiveDocuments();
    request.setKeha(new ReceiveDocumentsV4RequestType());
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    List<Document> docs = new ArrayList<Document>();
    Document doc = new Document();
    docs.add(doc);
    doc.addTransport(new Transport());
    doc.getTransports().get(0).addSender(new Sender());
    doc.getTransports().get(0).getSenders().get(0).setOrganisation(new Organisation());
    Document doc2 = new Document();
    doc2.addTransport(new Transport());
    doc2.getTransports().get(0).addSender(new Sender());
    doc2.getTransports().get(0).getSenders().get(0).setOrganisation(new Organisation());
    docs.add(doc2);
    when(documentRepository
        .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusId(
            Mockito.eq(false), Mockito.eq(senderOrg),
            Mockito.eq(StatusEnum.IN_PROCESS.getClassificatorId()), any(Pageable.class)))
                .thenReturn(docs);
    DecContainer container = new DecContainer();
    when(capsuleService.getContainerFromDocument(doc)).thenReturn(container);
    when(capsuleService.getContainerFromDocument(doc2)).thenReturn(container);
    when(convertationService.createDatahandlerFromList(any(List.class))).thenReturn(handler);
    ReceiveDocumentsResponse resp =
        soapService.receiveDocuments(request, senderMember, recipientMember);
    verify(capsuleService, times(2)).getContainerFromDocument(any(Document.class));
    verify(documentRepository, times(1))
        .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusId(
            Mockito.eq(false), Mockito.eq(senderOrg),
            Mockito.eq(StatusEnum.IN_PROCESS.getClassificatorId()), any(Pageable.class));
    assertEquals(handler, resp.getKeha().getHref());

  }

  @Test
  public void receiveDocumentsFolder() throws DhxException {
    DataHandler handler = Mockito.mock(DataHandler.class);
    ReceiveDocuments request = new ReceiveDocuments();
    request.setKeha(new ReceiveDocumentsV4RequestType());
    request.getKeha().setKaust("folder");
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    List<Document> docs = new ArrayList<Document>();
    Document doc = new Document();
    doc.addTransport(new Transport());
    doc.getTransports().get(0).addSender(new Sender());
    doc.getTransports().get(0).getSenders().get(0).setOrganisation(new Organisation());
    docs.add(doc);
    Document doc2 = new Document();
    doc2.addTransport(new Transport());
    doc2.getTransports().get(0).addSender(new Sender());
    doc2.getTransports().get(0).getSenders().get(0).setOrganisation(new Organisation());
    docs.add(doc2);
    Folder folder = new Folder();
    when(folderRepository.findByName("folder")).thenReturn(folder);
    when(documentRepository
        .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
            Mockito.eq(false), Mockito.eq(senderOrg),
            Mockito.eq(StatusEnum.IN_PROCESS.getClassificatorId()), Mockito.eq(folder),
            any(Pageable.class))).thenReturn(docs);
    DecContainer container = new DecContainer();
    when(capsuleService.getContainerFromDocument(doc)).thenReturn(container);
    when(capsuleService.getContainerFromDocument(doc2)).thenReturn(container);
    when(convertationService.createDatahandlerFromList(any(List.class))).thenReturn(handler);
    ReceiveDocumentsResponse resp =
        soapService.receiveDocuments(request, senderMember, recipientMember);
    verify(capsuleService, times(2)).getContainerFromDocument(any(Document.class));
    verify(documentRepository, times(1))
        .findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
            Mockito.eq(false), Mockito.eq(senderOrg),
            Mockito.eq(StatusEnum.IN_PROCESS.getClassificatorId()), Mockito.eq(folder),
            any(Pageable.class));
    verify(folderRepository, times(1)).findByName("folder");
    assertEquals(handler, resp.getKeha().getHref());

  }

  // not sender recipient, multiple recipients/documents, fault
  @Test
  public void markDocumentReceived() throws DhxException {
    MarkDocumentsReceived requestWrapper = new MarkDocumentsReceived();
    MarkDocumentsReceivedV3RequestType request = new MarkDocumentsReceivedV3RequestType();
    requestWrapper.setKeha(request);
    TagasisideType status = new TagasisideType();
    List<TagasisideType> statuss = new ArrayList<TagasisideType>();
    status.setDhlId(BigInteger.valueOf(10));
    status.setVastuvotjaStaatusId(BigInteger.valueOf(2));
    XMLGregorianCalendar cal = ConversionUtil.toGregorianCalendar(new Date());
    status.setStaatuseMuutmiseAeg(cal);
    statuss.add(status);
    request.setDokumendid(new Dokumendid());
    request.getDokumendid().setTagasisided(statuss);
    // request.setKaust("");
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    senderOrg.setOrganisationId(15);
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    Document doc = new Document();
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setOrganisation(senderOrg);
    doc.getTransports().get(0).addRecipient(recipient);
    when(documentRepository.findOne(10L)).thenReturn(doc);
    recipientMember.setServiceVersion("v3");
    soapService.markDocumentReceived(requestWrapper, senderMember, recipientMember, null);
    verify(documentRepository, times(1)).save(doc);
    verify(recipientRepository, times(1)).save(recipient);
    assertEquals(StatusEnum.RECEIVED.getClassificatorId(),
        doc.getTransports().get(0).getStatusId());
    assertEquals(StatusEnum.RECEIVED.getClassificatorId(), recipient.getStatusId());
    assertNotNull(recipient.getStatusChangeDate());
    assertEquals(new Integer(2), recipient.getRecipientStatusId());
    assertNotNull(recipient.getSendingEnd());
    assertNull(recipient.getFaultCode());
  }

  @Test
  public void markDocumentReceivedFault() throws DhxException {
    MarkDocumentsReceived requestWrapper = new MarkDocumentsReceived();
    MarkDocumentsReceivedV3RequestType request = new MarkDocumentsReceivedV3RequestType();
    requestWrapper.setKeha(request);
    TagasisideType status = new TagasisideType();
    List<TagasisideType> statuss = new ArrayList<TagasisideType>();
    status.setDhlId(BigInteger.valueOf(10));
    status.setVastuvotjaStaatusId(BigInteger.valueOf(2));
    Fault fault = new Fault();
    fault.setFaultcode("code");
    fault.setFaultdetail("detail");
    fault.setFaultstring("string");
    fault.setFaultactor("actor");
    status.setFault(fault);
    XMLGregorianCalendar cal = ConversionUtil.toGregorianCalendar(new Date());
    status.setStaatuseMuutmiseAeg(cal);
    statuss.add(status);
    request.setDokumendid(new Dokumendid());
    request.getDokumendid().setTagasisided(statuss);
    // request.setKaust("");
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    senderOrg.setOrganisationId(15);
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    Document doc = new Document();
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setOrganisation(senderOrg);
    doc.getTransports().get(0).addRecipient(recipient);
    when(documentRepository.findOne(10L)).thenReturn(doc);
    recipientMember.setServiceVersion("v3");
    soapService.markDocumentReceived(requestWrapper, senderMember, recipientMember, null);
    verify(recipientRepository, times(1)).save(recipient);
    assertEquals(StatusEnum.FAILED.getClassificatorId(),
        doc.getTransports().get(0).getStatusId());
    assertEquals(StatusEnum.FAILED.getClassificatorId(), recipient.getStatusId());
    assertNotNull(recipient.getStatusChangeDate());
    assertEquals(new Integer(2), recipient.getRecipientStatusId());
    assertNotNull(recipient.getSendingEnd());
    assertEquals("code", recipient.getFaultCode());
    assertEquals("detail", recipient.getFaultDetail());
    assertEquals("string", recipient.getFaultString());
    assertEquals("actor", recipient.getFaultActor());

  }

  @Test
  public void markDocumentReceivedRecipientNotFound() throws DhxException {
    MarkDocumentsReceived requestWrapper = new MarkDocumentsReceived();
    MarkDocumentsReceivedV3RequestType request = new MarkDocumentsReceivedV3RequestType();
    requestWrapper.setKeha(request);
    TagasisideType status = new TagasisideType();
    List<TagasisideType> statuss = new ArrayList<TagasisideType>();
    status.setDhlId(BigInteger.valueOf(10));
    status.setVastuvotjaStaatusId(BigInteger.valueOf(2));
    Fault fault = new Fault();
    fault.setFaultcode("code");
    fault.setFaultdetail("detail");
    fault.setFaultstring("string");
    fault.setFaultactor("actor");
    status.setFault(fault);
    XMLGregorianCalendar cal = ConversionUtil.toGregorianCalendar(new Date());
    status.setStaatuseMuutmiseAeg(cal);
    statuss.add(status);
    request.setDokumendid(new Dokumendid());
    request.getDokumendid().setTagasisided(statuss);
    // request.setKaust("");
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    senderOrg.setOrganisationId(15);
    Organisation recipientOrg = new Organisation();
    recipientOrg.setOrganisationId(20);
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    Document doc = new Document();
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setOrganisation(recipientOrg);
    doc.getTransports().get(0).addRecipient(recipient);
    when(documentRepository.findOne(10L)).thenReturn(doc);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("That document is not sent to recipient organisation");
    recipientMember.setServiceVersion("v3");
    soapService.markDocumentReceived(requestWrapper, senderMember, recipientMember, null);
    verify(recipientRepository, times(0)).save(recipient);

  }

  @Test
  public void markDocumentReceivedMultiple() throws DhxException {
    MarkDocumentsReceived requestWrapper = new MarkDocumentsReceived();
    MarkDocumentsReceivedV3RequestType request = new MarkDocumentsReceivedV3RequestType();
    requestWrapper.setKeha(request);
    TagasisideType status = new TagasisideType();
    List<TagasisideType> statuss = new ArrayList<TagasisideType>();
    status.setDhlId(BigInteger.valueOf(10));
    status.setVastuvotjaStaatusId(BigInteger.valueOf(2));
    XMLGregorianCalendar cal = ConversionUtil.toGregorianCalendar(new Date());
    status.setStaatuseMuutmiseAeg(cal);
    statuss.add(status);

    TagasisideType status2 = new TagasisideType();
    status2.setDhlId(BigInteger.valueOf(11));
    status2.setVastuvotjaStaatusId(BigInteger.valueOf(2));
    XMLGregorianCalendar cal2 = ConversionUtil.toGregorianCalendar(new Date());
    status2.setStaatuseMuutmiseAeg(cal2);
    Fault fault = new Fault();
    fault.setFaultcode("code");
    fault.setFaultdetail("detail");
    fault.setFaultstring("string");
    fault.setFaultactor("actor");
    status2.setFault(fault);
    statuss.add(status2);
    request.setDokumendid(new Dokumendid());
    request.getDokumendid().setTagasisided(statuss);
    // request.setKaust("");
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Organisation senderOrg = new Organisation();
    senderOrg.setOrganisationId(15);
    when(organisationRepository.findByRegistrationCodeAndSubSystem(senderMember.getMemberCode(),
        senderMember.getSubsystemCode())).thenReturn(senderOrg);
    Document doc = new Document();
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setOrganisation(senderOrg);
    doc.getTransports().get(0).addRecipient(recipient);
    when(documentRepository.findOne(10L)).thenReturn(doc);

    Document doc2 = new Document();
    doc2.addTransport(new Transport());
    doc2.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient2 = new Recipient();
    recipient2.setOrganisation(senderOrg);
    doc2.getTransports().get(0).addRecipient(recipient2);

    Organisation otherOrg = new Organisation();
    otherOrg.setOrganisationId(18);
    Recipient recipient3 = new Recipient();
    recipient3.setOrganisation(otherOrg);
    doc2.getTransports().get(0).addRecipient(recipient3);
    when(documentRepository.findOne(11L)).thenReturn(doc2);
    recipientMember.setServiceVersion("v3");
    soapService.markDocumentReceived(requestWrapper, senderMember, recipientMember, null);
    verify(documentRepository, times(1)).save(doc);
    verify(documentRepository, times(0)).save(doc2);
    verify(recipientRepository, times(1)).save(recipient);
    verify(recipientRepository, times(1)).save(recipient2);
    verify(recipientRepository, times(0)).save(recipient3);

  }

  @Test
  public void getSendStatus() throws DhxException, ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    GetSendStatus request = new GetSendStatus();
    request.setKeha(new GetSendStatusV2RequestType());
    request.getKeha().setDokumendid(new Base64BinaryType());
    request.getKeha().setStaatuseAjalugu(true);
    // <item> <dhl_id>59</dhl_id></item>
    String dhlIds = "H4sIAAAAAAAAALPJLEnNtVOwScnIic9MsTO1tNGHMm30wVIAQi7PIiEAAAA=";
    DataHandler handler = new DataHandler(new ByteArrayDataSource(dhlIds.getBytes(), ""));
    request.getKeha().getDokumendid().setHref(handler);
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Document doc = new Document();
    doc.setDocumentId(10L);
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setRecipientStatusId(2);
    recipient.setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    recipient.setPersonalcode("pcode");
    recipient.setStructuralUnit("sunit");
    Organisation recOrg = new Organisation();
    recOrg.setOrganisationId(15);
    recOrg.setRegistrationCode("regCode");
    recOrg.setName("orgName");
    Date start = sdf.parse("06.12.2016 12:32");
    Date end = sdf.parse("08.12.2016 12:32");
    recipient.setSendingStart(new Timestamp(start.getTime()));
    recipient.setSendingEnd(new Timestamp(end.getTime()));
    StatusHistory history = new StatusHistory();
    history.setStatusHistoryId(88);
    Date changeDate = sdf.parse("07.12.2016 12:32");
    history.setStatusChangeDate(new Timestamp(changeDate.getTime()));
    history.setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    history.setRecipientStatusId(3);
    recipient.addStatusHistory(history);
    recipient.setOrganisation(recOrg);
    doc.getTransports().get(0).addRecipient(recipient);

    List<Document> docs = new ArrayList<Document>();
    docs.add(doc);
    DataHandler handlerMock = Mockito.mock(DataHandler.class);
    when(documentRepository.findByDocumentIdIn(any(List.class))).thenReturn(docs);
    when(convertationService.createDatahandlerFromList(any(List.class))).thenReturn(handlerMock);
    when(persistenceService.toDvkCapsuleAddressee(Mockito.anyString(), Mockito.anyString()))
        .thenReturn("regCode");
    GetSendStatusResponse resp =
        soapService.getSendStatus(request, senderMember, recipientMember, null);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    Mockito.verify(convertationService).createDatahandlerFromList(argument.capture());
    List<GetSendStatusV2ResponseTypeUnencoded.Item> items = argument.getValue();
    assertEquals(handlerMock, resp.getKeha().getHref());
    assertEquals(1, items.size());
    assertEquals("10", items.get(0).getDhlId());
    assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getOlek());
    assertEquals(1, items.get(0).getEdastus().size());
    assertEquals("xtee", items.get(0).getEdastus().get(0).getMeetod());

    assertEquals("06.12.2016 12:32",
        sdf.format(ConversionUtil.toDate(items.get(0).getEdastus().get(0).getEdastatud())));
    assertEquals("08.12.2016 12:32",
        sdf.format(ConversionUtil.toDate(items.get(0).getEdastus().get(0).getLoetud())));
    assertEquals("06.12.2016 12:32",
        sdf.format(ConversionUtil.toDate(items.get(0).getEdastus().get(0).getSaadud())));

    assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(),
        items.get(0).getEdastus().get(0).getStaatus());
    assertEquals(BigInteger.valueOf(2),
        items.get(0).getEdastus().get(0).getVastuvotjaStaatusId());
    assertEquals("regCode", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
    assertEquals("pcode", items.get(0).getEdastus().get(0).getSaaja().getIsikukood());
    assertEquals("orgName", items.get(0).getEdastus().get(0).getSaaja().getAsutuseNimi());
    assertNull(items.get(0).getEdastus().get(0).getFault());
    assertEquals(1, items.get(0).getStaatuseAjalugu().getStatus().size());
    assertNull(items.get(0).getStaatuseAjalugu().getStatus().get(0).getFault());
    assertEquals(BigInteger.valueOf(3),
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getVastuvotjaStaatusId());
    assertEquals(BigInteger.valueOf(88),
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getStaatuseAjaluguId());
    assertEquals(StatusType.SAATMISEL,
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getStaatus());
    assertEquals("regCode",
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getSaaja().getRegnr());
    assertEquals("pcode",
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getSaaja().getIsikukood());
    assertEquals("sunit",
        items.get(0).getStaatuseAjalugu().getStatus().get(0).getSaaja()
            .getAllyksuseLyhinimetus());
    assertEquals("07.12.2016 12:32", sdf.format(
        ConversionUtil.toDate(
            items.get(0).getStaatuseAjalugu().getStatus().get(0).getStaatuseMuutmiseAeg())));
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void getSendStatusNoHistory() throws DhxException, ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    GetSendStatus request = new GetSendStatus();
    request.setKeha(new GetSendStatusV2RequestType());
    request.getKeha().setDokumendid(new Base64BinaryType());
    request.getKeha().setStaatuseAjalugu(false);
    // <item> <dhl_id>59</dhl_id></item>
    String dhlIds = "H4sIAAAAAAAAALPJLEnNtVOwScnIic9MsTO1tNGHMm30wVIAQi7PIiEAAAA=";
    DataHandler handler = new DataHandler(new ByteArrayDataSource(dhlIds.getBytes(), ""));
    request.getKeha().getDokumendid().setHref(handler);
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    Document doc = new Document();
    doc.setDocumentId(10L);
    doc.addTransport(new Transport());
    doc.getTransports().get(0).setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    Recipient recipient = new Recipient();
    recipient.setRecipientStatusId(2);
    recipient.setStatusId(StatusEnum.IN_PROCESS.getClassificatorId());
    recipient.setPersonalcode("pcode");
    recipient.setStructuralUnit("sunit");
    Organisation recOrg = new Organisation();
    recOrg.setOrganisationId(15);
    recOrg.setRegistrationCode("regCode");
    recOrg.setName("orgName");
    Date start = sdf.parse("06.12.2016 12:32");
    Date end = sdf.parse("08.12.2016 12:32");
    recipient.setSendingStart(new Timestamp(start.getTime()));
    recipient.setSendingEnd(new Timestamp(end.getTime()));
    StatusHistory history = new StatusHistory();
    recipient.addStatusHistory(history);
    recipient.setOrganisation(recOrg);
    doc.getTransports().get(0).addRecipient(recipient);

    List<Document> docs = new ArrayList<Document>();
    docs.add(doc);
    when(documentRepository.findByDocumentIdIn(any(List.class))).thenReturn(docs);
    soapService.getSendStatus(request, senderMember, recipientMember, null);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    Mockito.verify(convertationService).createDatahandlerFromList(argument.capture());
    List<GetSendStatusV2ResponseTypeUnencoded.Item> items = argument.getValue();
    assertNull(items.get(0).getStaatuseAjalugu());
  }

  @Test
  @Ignore
  public void getSendingOptions() throws DhxException {
    InternalXroadMember senderMember = getMember("sender", null);
    InternalXroadMember recipientMember = getMember("recipient", null);
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member = getMember("code", null);
    members.add(member);

    member = getMember("code", null);
    member.setSubsystemCode("DHX.system");
    member.setName("memberName");
    members.add(member);

    member = getMember("code", null);
    member.setSubsystemCode("DHX.special");
    members.add(member);
    member.setName("memberName2");

    DhxRepresentee representee = new DhxRepresentee("reprCode", new Date(), null, "Name", null);
    member = getMember("code", representee);
    members.add(member);

    representee = new DhxRepresentee("reprCode2", new Date(),
        new Date(new Date().getTime() - 1000), "Name2", null);
    member = getMember("code", representee);
    members.add(member);

    representee = new DhxRepresentee("reprCode3", new Date(), null, "Name3", "system");
    member = getMember("code", representee);
    members.add(member);

    representee = new DhxRepresentee("reprCode4", new Date(), null, "Name4", "specialrepr");
    member = getMember("code", representee);
    members.add(member);

    when(persistenceService.isSpecialOrganisation("special")).thenReturn(true);
    when(persistenceService.isSpecialOrganisation("specialrepr")).thenReturn(true);
    when(addressService.getAdresseeList()).thenReturn(members);
    DataHandler handler = Mockito.mock(DataHandler.class);
    when(convertationService.createDatahandlerFromObject(any(InstitutionArrayType.class)))
        .thenReturn(handler);
    GetSendingOptions request = new GetSendingOptions();
    request.setKeha(new GetSendingOptionsV2RequestType());
    GetSendingOptionsResponse resp =
        soapService.getSendingOptions(request, senderMember, recipientMember, null);
    ArgumentCaptor<InstitutionArrayType> argument =
        ArgumentCaptor.forClass(InstitutionArrayType.class);
    Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
    InstitutionArrayType items = argument.getValue();
    verify(addressService, times(1)).getAdresseeList();
    //assertEquals(handler, resp.getKeha().getHref());
    assertEquals(6, items.getAsutus().size());

    assertEquals("dhl", items.getAsutus().get(0).getSaatmine().getSaatmisviis().get(0));

    assertEquals("system.code", items.getAsutus().get(1).getRegnr());
    assertEquals("memberName", items.getAsutus().get(1).getNimi());

    assertEquals("special", items.getAsutus().get(2).getRegnr());
    assertEquals("memberName2", items.getAsutus().get(2).getNimi());

    assertEquals("reprCode", items.getAsutus().get(3).getRegnr());
    assertEquals("Name", items.getAsutus().get(3).getNimi());

    assertEquals("system.reprCode3", items.getAsutus().get(4).getRegnr());
    assertEquals("Name3", items.getAsutus().get(4).getNimi());

    assertEquals("specialrepr", items.getAsutus().get(5).getRegnr());
    assertEquals("Name4", items.getAsutus().get(5).getNimi());
  }
}

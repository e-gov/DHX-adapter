package ee.ria.dhx.server.endpoint;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;
import static org.springframework.ws.test.server.RequestCreators.withSoapEnvelope;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.RepoFactory4Test;
import ee.ria.dhx.server.TestApp;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.enumeration.RecipientStatusEnum;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.DocumentRepository;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.service.ConvertationService;
import ee.ria.dhx.server.service.SoapService;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.server.types.ee.riik.schemas.dhl.Edastus;

import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.DocumentRefsArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.DocumentsArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedV3RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.TagasisideType;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.AccessConditionType;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.OrganisationType;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.representation.XRoadRepresentedPartyType;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.ws.test.client.RequestMatchers;
import org.springframework.ws.test.client.ResponseCreators;
/*
 * import org.springframework.ws.test.server.MockWebServiceClient; import
 * org.springframework.ws.test.server.RequestCreator; import
 * org.springframework.ws.test.server.RequestCreators;
 */
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.ws.test.client.RequestMatchers;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.RequestCreators;
import org.springframework.ws.test.server.ResponseMatchers;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.transaction.Transactional;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

// import org.springframework.test.context.junit4.SpringRunner;

/**
 * Tests on DhxEndpoint. Real XML-s are sent to endpoint and received response
 * is being validated.
 * 
 * @author Aleksei Kokarev
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:test-application.properties")
@ContextConfiguration(classes = { TestApp.class, RepoFactory4Test.class })
@Slf4j
@Transactional
// @WebIntegrationTest("server.port:9000")
public class ServerEndpointITest {

	@Autowired
	private ApplicationContext applicationContext;

	private MockWebServiceClient mockClient;

	private MockWebServiceServer mockServer;

	@Autowired
	Jaxb2Marshaller marshaller;

	@Autowired
	ConvertationService convertationService;

	@Autowired
	AddressService addressService;

	@Autowired
	OrganisationRepository organisationRepository;

	@Autowired
	FolderRepository folderRepository;

	String resourceFolder = "endpoint/";

	@Autowired
	SoapService soapService;

	@Autowired
	DocumentRepository documentRepository;

	@Autowired
	DhxMarshallerService dhxMarshallerService;

	/*
	 * Unmarshaller unmarshaller;
	 * 
	 * JAXBContext context;
	 */

	@Before
	public void init() throws DhxException, IOException {
		mockClient = MockWebServiceClient.createClient(applicationContext);
		mockServer = MockWebServiceServer.createServer(applicationContext);
		List<InternalXroadMember> members = createMemberList();
		addRegularMember(members);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		Source responseEnvelope = new StreamSource(
				new ClassPathResource(resourceFolder + "representationList_response.xml").getFile());
		mockServer.expect(RequestMatchers.xpath("//ns9:representationList[1]", getDhxNamespaceMap()).exists())
				.andRespond(ResponseCreators.withSoapEnvelope(responseEnvelope));
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		addressService.renewAddressList();
		Mockito.reset(convertationService);

	}

	// method creates source, and mock the request of the marshaller, because we
	// need to set
	// attachment aswell, but mock client does not support that
	/*
	 * private Source createSource(Object body) throws Exception {
	 * Mockito.doReturn(body).when(marshaller).unmarshal(any(Source.class),
	 * any(MimeContainer.class)); return requestEnvelope; }
	 */

	private Map<String, String> getDhxNamespaceMap() {
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("ns9", "http://dhx.x-road.eu/producer");
		return namespaces;
	}

	private Map<String, String> getDhlNamespaceMap() {
		Map<String, String> namespaces = new HashMap<String, String>();
		namespaces.put("ns4", "http://producers.dhl.xrd.riik.ee/producer/dhl");
		return namespaces;
	}

	private List<InternalXroadMember> createMemberList() throws DhxException {
		List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
		return members;
	}

	private void addRegularMember(List<InternalXroadMember> members) {
		InternalXroadMember member = new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", null);
		members.add(member);
	}

	private SendDocuments getSendDocumentsRequest(DecContainer... containers) throws DhxException {
		SendDocuments request = new SendDocuments();
		request.setKeha(new SendDocumentsV4RequestType());
		request.getKeha().setDokumendid(new Base64BinaryType());
		request.getKeha().getDokumendid().setHref(getSendDocumentsAttachment(containers));
		return request;
	}

	private DecContainer getContainer(String senderOrganisationId, String... recipientOrganisationIds)
			throws DhxException {
		DecContainer container = new DecContainer();
		container.setTransport(new DecContainer.Transport());
		container.getTransport().setDecSender(new DecContainer.Transport.DecSender());
		container.getTransport().getDecSender().setOrganisationCode(senderOrganisationId);
		for (String recipientOrgId : recipientOrganisationIds) {
			DecContainer.Transport.DecRecipient rec = new DecContainer.Transport.DecRecipient();
			rec.setOrganisationCode(recipientOrgId);
			container.getTransport().getDecRecipient().add(rec);
			DecContainer.Recipient recip = new DecContainer.Recipient();
			OrganisationType org = new OrganisationType();
			org.setOrganisationCode(recipientOrgId);
			org.setName("ORg");
			recip.setOrganisation(org);
			container.getRecipient().add(recip);

		}
		container.setRecordMetadata(new DecContainer.RecordMetadata());
		container.getRecordMetadata().setRecordGuid("25892e17-80f6-415f-9c65-7395632f0211");
		container.getRecordMetadata().setRecordType("Kiri");
		container.getRecordMetadata().setRecordOriginalIdentifier("12.1/125");
		container.getRecordMetadata().setRecordDateRegistered(ConversionUtil.toGregorianCalendar(new Date()));
		container.getRecordMetadata().setRecordTitle("Ttile");
		container.getRecordMetadata().setRecordLanguage("EE");
		container.getRecordMetadata().setRecordAbstract("Ttile");
		container.setAccess(new DecContainer.Access());
		container.getAccess().setAccessConditionsCode(AccessConditionType.AVALIK);
		container.getFile().add(new DecContainer.File());
		container.getFile().get(0).setFileGuid("25892e17-80f6-415f-9c65-7395632f0211");
		container.getFile().get(0).setFileName("name");
		container.getFile().get(0).setMimeType("text");
		container.getFile().get(0).setFileSize(BigInteger.valueOf(1212));
		container.getFile().get(0).setRecordMainComponent(true);
		container.getFile().get(0).setZipBase64Content("content");
		return container;
	}

	private DataHandler getSendDocumentsAttachment(DecContainer... containers) throws DhxException {
		DocumentsArrayType docs = new DocumentsArrayType();
		for (DecContainer cont : containers) {
			docs.getDecContainer().add(cont);
		}
		return IntegrationTestHelper.createDatahandlerFromList(docs.getDecContainer());

	}

	/**
	 * Tests whole flow. Starts with sendDocuments service, then document is
	 * sent to DHX and send status is checked.
	 * 
	 * @throws DhxException
	 */
	@Test
	public void sendDocumentsNormal() throws DhxException {
		DecContainer cont = getContainer("30000001", "70000004");
		SendDocuments request = getSendDocumentsRequest(cont);
		// reset because we just did a request into that service.
		Mockito.reset(convertationService);
		Mockito.doReturn(request).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		XRoadClientIdentifierType client = IntegrationTestHelper.getClient("30000001");
		XRoadServiceIdentifierType service = IntegrationTestHelper.getService("40000001", "v4");
		// XRoadRepresentedPartyType representee =
		// IntegrationTestHelper.getRepresentee("");

		// send Document
		Source envelope = IntegrationTestHelper.getEnvelope(client, service, null, new SendDocuments());
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(envelope))
				.andExpect(ResponseMatchers.xpath("//ns4:sendDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<SendDocumentsV4ResponseTypeUnencoded.Keha> argument = ArgumentCaptor
				.forClass(SendDocumentsV4ResponseTypeUnencoded.Keha.class);
		Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
		SendDocumentsV4ResponseTypeUnencoded.Keha keha = argument.getValue();
		assertEquals(1, keha.getDhlId().size());
		Long docId = Long.parseLong(keha.getDhlId().get(0));
		Document doc = documentRepository.findOne(docId);
		assertEquals(true, doc.getOutgoingDocument());
		assertEquals("/", doc.getFolder().getName());
		assertEquals(client.getMemberCode(), doc.getOrganisation().getRegistrationCode());

		assertEquals(1, doc.getTransports().size());
		assertEquals(1, doc.getTransports().get(0).getSenders().size());
		assertEquals(1, doc.getTransports().get(0).getRecipients().size());
		assertEquals(client.getMemberCode(),
				doc.getTransports().get(0).getSenders().get(0).getOrganisation().getRegistrationCode());
		assertEquals("70000004",
				doc.getTransports().get(0).getRecipients().get(0).getOrganisation().getRegistrationCode());
		// check document in db

		// get send status
		GetSendStatus status = new GetSendStatus();
		status.setKeha(new GetSendStatusV2RequestType());
		DocumentRefsArrayType docIds = new DocumentRefsArrayType();
		docIds.getDhlId().add(docId.toString());
		status.getKeha().setDokumendid(new Base64BinaryType());
		status.getKeha().getDokumendid().setHref(convertationService.createDatahandlerFromObject(docIds));
		Mockito.doReturn(status).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		service.setServiceVersion("v2");
		Source getSendStatusEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, new GetSendStatus());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(getSendStatusEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendStatusResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<List> getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		List<GetSendStatusV2ResponseTypeUnencoded.Item> items = getSendStatusArgument.getValue();

		assertEquals(1, items.size());
		assertEquals(docId.toString(), items.get(0).getDhlId());
		assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getOlek());
		assertEquals(1, items.get(0).getEdastus().size());
		assertEquals("70000004", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
		assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getEdastus().get(0).getStaatus());
		assertNull(items.get(0).getEdastus().get(0).getFault());
		assertNull(items.get(0).getEdastus().get(0).getLoetud());

		// send document to DHX
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		SendDocumentResponse sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId1");
		Source sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null,
				sendDocumentResponse);
		MockWebServiceServer mockServerSendDOcument = MockWebServiceServer.createServer(applicationContext);
		mockServerSendDOcument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(0).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:DHXVersion[1]", getDhxNamespaceMap())
						.evaluatesTo("1.0"))
				.andExpect(RequestMatchers
						.xpath("//ns9:sendDocument[1]/ns9:documentAttachment[1]", getDhxNamespaceMap()).exists())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));
		soapService.sendDocumentsToDhx();
		mockServerSendDOcument.verify();
		doc = documentRepository.findOne(docId);
		assertEquals("receiptId1", doc.getTransports().get(0).getRecipients().get(0).getDhxExternalReceiptId());

		// get send status
		status = new GetSendStatus();
		status.setKeha(new GetSendStatusV2RequestType());
		docIds = new DocumentRefsArrayType();
		docIds.getDhlId().add(docId.toString());
		status.getKeha().setDokumendid(new Base64BinaryType());
		status.getKeha().getDokumendid().setHref(convertationService.createDatahandlerFromObject(docIds));
		Mockito.doReturn(status).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		service.setServiceVersion("v2");
		getSendStatusEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, new GetSendStatus());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(getSendStatusEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendStatusResponse[1]", getDhlNamespaceMap()).exists());
		getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		items = getSendStatusArgument.getValue();

		assertEquals(1, items.size());
		assertEquals(docId.toString(), items.get(0).getDhlId());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), items.get(0).getOlek());
		assertEquals(1, items.get(0).getEdastus().size());
		assertEquals("70000004", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), items.get(0).getEdastus().get(0).getStaatus());
		assertNull(items.get(0).getEdastus().get(0).getFault());
		assertNotNull(items.get(0).getEdastus().get(0).getLoetud());

		
		
		// send document to DHX. just to check that no new requests will be sent.
		soapService.sendDocumentsToDhx();

	}

	/**
	 * Tests whole flow. Starts with sendDocuments service, then document is
	 * sent to DHX and send status is checked. In sendDocuments there are
	 * multiple reciepient of different types (representees, with subsystems
	 * etc).
	 * 
	 * @throws DhxException
	 */
	@Test
	public void sendDocumentsMultipleRecipients() throws DhxException {
		DecContainer cont = getContainer("30000001", "70000004", "raamatupidamine.30000001", "adit", "500",
				"system.500", "rt");
		SendDocuments request = getSendDocumentsRequest(cont);
		request.getKeha().setKaust("folder");
		// reset because we just did a request into that service.
		Mockito.reset(convertationService);
		Mockito.doReturn(request).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		XRoadClientIdentifierType client = IntegrationTestHelper.getClient("30000001");
		XRoadServiceIdentifierType service = IntegrationTestHelper.getService("40000001", "v4");

		// send Document
		Source envelope = IntegrationTestHelper.getEnvelope(client, service, null, new SendDocuments());
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(envelope))
				.andExpect(ResponseMatchers.xpath("//ns4:sendDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<SendDocumentsV4ResponseTypeUnencoded.Keha> argument = ArgumentCaptor
				.forClass(SendDocumentsV4ResponseTypeUnencoded.Keha.class);
		Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
		SendDocumentsV4ResponseTypeUnencoded.Keha keha = argument.getValue();
		assertEquals(1, keha.getDhlId().size());
		Long docId = Long.parseLong(keha.getDhlId().get(0));
		Document doc = documentRepository.findOne(docId);
		assertEquals(true, doc.getOutgoingDocument());
		assertEquals("folder", doc.getFolder().getName());
		assertEquals(client.getMemberCode(), doc.getOrganisation().getRegistrationCode());

		assertEquals(1, doc.getTransports().size());
		assertEquals(1, doc.getTransports().get(0).getSenders().size());
		assertEquals(6, doc.getTransports().get(0).getRecipients().size());

		assertEquals(client.getMemberCode(),
				doc.getTransports().get(0).getSenders().get(0).getOrganisation().getRegistrationCode());
		assertEquals("70000004",
				doc.getTransports().get(0).getRecipients().get(0).getOrganisation().getRegistrationCode());
		assertEquals("30000001",
				doc.getTransports().get(0).getRecipients().get(1).getOrganisation().getRegistrationCode());
		assertEquals("DHX.raamatupidamine",
				doc.getTransports().get(0).getRecipients().get(1).getOrganisation().getSubSystem());
		assertEquals("70000004",
				doc.getTransports().get(0).getRecipients().get(2).getOrganisation().getRegistrationCode());
		assertEquals("DHX.adit", doc.getTransports().get(0).getRecipients().get(2).getOrganisation().getSubSystem());
		assertEquals("500", doc.getTransports().get(0).getRecipients().get(3).getOrganisation().getRegistrationCode());
		assertEquals("500", doc.getTransports().get(0).getRecipients().get(4).getOrganisation().getRegistrationCode());
		assertEquals("system", doc.getTransports().get(0).getRecipients().get(4).getOrganisation().getSubSystem());
		assertEquals("510", doc.getTransports().get(0).getRecipients().get(5).getOrganisation().getRegistrationCode());
		assertEquals("rt", doc.getTransports().get(0).getRecipients().get(5).getOrganisation().getSubSystem());

		// check document in db

		// get send status
		GetSendStatus status = new GetSendStatus();
		status.setKeha(new GetSendStatusV2RequestType());
		DocumentRefsArrayType docIds = new DocumentRefsArrayType();
		docIds.getDhlId().add(docId.toString());
		status.getKeha().setDokumendid(new Base64BinaryType());
		status.getKeha().getDokumendid().setHref(convertationService.createDatahandlerFromObject(docIds));
		Mockito.doReturn(status).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		service.setServiceVersion("v2");
		Source getSendStatusEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, new GetSendStatus());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(getSendStatusEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendStatusResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<List> getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		List<GetSendStatusV2ResponseTypeUnencoded.Item> items = getSendStatusArgument.getValue();

		assertEquals(1, items.size());
		assertEquals(docId.toString(), items.get(0).getDhlId());
		assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getOlek());
		assertEquals(6, items.get(0).getEdastus().size());

		assertEquals("70000004", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
		assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getEdastus().get(0).getStaatus());
		assertNull(items.get(0).getEdastus().get(0).getFault());
		assertNull(items.get(0).getEdastus().get(0).getLoetud());

		// send document to DHX
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		// mock first response
		SendDocumentResponse sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId1");
		Source sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null,
				sendDocumentResponse);
		MockWebServiceServer mockServerSendD0cument = MockWebServiceServer.createServer(applicationContext);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(0).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock second response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId2");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(1).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock third response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId3");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(2).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock fourth response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId4");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(3).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.evaluatesTo("500"))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock fifth response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId5");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(4).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.evaluatesTo("500"))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.evaluatesTo("system"))
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock sixth response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId6");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(5).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.evaluatesTo("510"))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.evaluatesTo("rt"))
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		soapService.sendDocumentsToDhx();
		mockServerSendD0cument.verify();
		doc = documentRepository.findOne(docId);
		assertEquals("receiptId1", doc.getTransports().get(0).getRecipients().get(0).getDhxExternalReceiptId());

		// get send status
		status = new GetSendStatus();
		status.setKeha(new GetSendStatusV2RequestType());
		docIds = new DocumentRefsArrayType();
		docIds.getDhlId().add(docId.toString());
		status.getKeha().setDokumendid(new Base64BinaryType());
		status.getKeha().getDokumendid().setHref(convertationService.createDatahandlerFromObject(docIds));
		Mockito.doReturn(status).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		service.setServiceVersion("v2");
		getSendStatusEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, new GetSendStatus());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(getSendStatusEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendStatusResponse[1]", getDhlNamespaceMap()).exists());
		getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		items = getSendStatusArgument.getValue();

		assertEquals(1, items.size());
		assertEquals(docId.toString(), items.get(0).getDhlId());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), items.get(0).getOlek());
		assertEquals(6, items.get(0).getEdastus().size());

		assertEquals("70000004", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), items.get(0).getEdastus().get(0).getStaatus());
		assertNull(items.get(0).getEdastus().get(0).getFault());
		assertNotNull(items.get(0).getEdastus().get(0).getLoetud());

		Edastus edastus = items.get(0).getEdastus().get(1);
		assertEquals("raamatupidamine.30000001", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), edastus.getStaatus());
		assertNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());

		edastus = items.get(0).getEdastus().get(2);
		assertEquals("adit", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), edastus.getStaatus());
		assertNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());

		edastus = items.get(0).getEdastus().get(3);
		assertEquals("500", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), edastus.getStaatus());
		assertNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());

		edastus = items.get(0).getEdastus().get(4);
		assertEquals("system.500", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), edastus.getStaatus());
		assertNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());

		edastus = items.get(0).getEdastus().get(5);
		assertEquals("rt", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), edastus.getStaatus());
		assertNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());

	}

	/**
	 * Tests whole flow. Starts with sendDocuments service, then document is
	 * sent to DHX and send status is checked. Document sending to DHX will get
	 * an error. etc).
	 * 
	 * @throws DhxException
	 */
	@Test
	public void sendDocumentsFailed() throws DhxException {
		DecContainer cont = getContainer("30000001", "70000004", "raamatupidamine.30000001");
		SendDocuments request = getSendDocumentsRequest(cont);
		request.getKeha().setKaust("folder");
		// reset because we just did a request into that service.
		Mockito.reset(convertationService);
		Mockito.doReturn(request).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		XRoadClientIdentifierType client = IntegrationTestHelper.getClient("30000001");
		XRoadServiceIdentifierType service = IntegrationTestHelper.getService("40000001", "v4");
		// XRoadRepresentedPartyType representee =
		// IntegrationTestHelper.getRepresentee("");

		// send Document
		Source envelope = IntegrationTestHelper.getEnvelope(client, service, null, new SendDocuments());
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(envelope))
				.andExpect(ResponseMatchers.xpath("//ns4:sendDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<SendDocumentsV4ResponseTypeUnencoded.Keha> argument = ArgumentCaptor
				.forClass(SendDocumentsV4ResponseTypeUnencoded.Keha.class);
		Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
		SendDocumentsV4ResponseTypeUnencoded.Keha keha = argument.getValue();
		assertEquals(1, keha.getDhlId().size());
		Long docId = Long.parseLong(keha.getDhlId().get(0));
		Document doc = documentRepository.findOne(docId);
		assertEquals(true, doc.getOutgoingDocument());
		assertEquals("folder", doc.getFolder().getName());
		assertEquals(client.getMemberCode(), doc.getOrganisation().getRegistrationCode());

		assertEquals(1, doc.getTransports().size());
		assertEquals(1, doc.getTransports().get(0).getSenders().size());
		assertEquals(2, doc.getTransports().get(0).getRecipients().size());

		// send document to DHX
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		// mock first response
		SendDocumentResponse sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setReceiptId("receiptId1");
		Source sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null,
				sendDocumentResponse);
		MockWebServiceServer mockServerSendD0cument = MockWebServiceServer.createServer(applicationContext);
		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(0).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock second response
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setFault(new Fault());
		sendDocumentResponse.getFault().setFaultCode("Fault code");
		sendDocumentResponse.getFault().setFaultString("Fault string");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);

		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(1).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		// mock second response retry.
		sendDocumentResponse = new SendDocumentResponse();
		sendDocumentResponse.setFault(new Fault());
		sendDocumentResponse.getFault().setFaultCode("Fault code");
		sendDocumentResponse.getFault().setFaultString("Fault string");
		sendDocumentResponseEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, sendDocumentResponse);

		mockServerSendD0cument
				.expect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:consignmentId[1]", getDhxNamespaceMap())
						.evaluatesTo(doc.getTransports().get(0).getRecipients().get(1).getRecipientId().toString()))
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipient[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andExpect(RequestMatchers.xpath("//ns9:sendDocument[1]/ns9:recipientSystem[1]", getDhxNamespaceMap())
						.doesNotExist())
				.andRespond(ResponseCreators.withSoapEnvelope(sendDocumentResponseEnvelope));

		soapService.sendDocumentsToDhx();
		mockServerSendD0cument.verify();
		doc = documentRepository.findOne(docId);
		assertEquals("receiptId1", doc.getTransports().get(0).getRecipients().get(0).getDhxExternalReceiptId());

		// get send status
		GetSendStatus status = new GetSendStatus();
		status.setKeha(new GetSendStatusV2RequestType());
		DocumentRefsArrayType docIds = new DocumentRefsArrayType();
		docIds.getDhlId().add(docId.toString());
		status.getKeha().setDokumendid(new Base64BinaryType());
		status.getKeha().getDokumendid().setHref(convertationService.createDatahandlerFromObject(docIds));
		Mockito.doReturn(status).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		service.setServiceVersion("v2");
		Source getSendStatusEnvelope = IntegrationTestHelper.getEnvelope(client, service, null, new GetSendStatus());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(getSendStatusEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendStatusResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<List> getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		List<GetSendStatusV2ResponseTypeUnencoded.Item> items = getSendStatusArgument.getValue();

		assertEquals(1, items.size());
		assertEquals(docId.toString(), items.get(0).getDhlId());
		assertEquals(StatusEnum.IN_PROCESS.getClassificatorName(), items.get(0).getOlek());
		assertEquals(2, items.get(0).getEdastus().size());

		assertEquals("70000004", items.get(0).getEdastus().get(0).getSaaja().getRegnr());
		assertEquals(StatusEnum.RECEIVED.getClassificatorName(), items.get(0).getEdastus().get(0).getStaatus());
		assertNull(items.get(0).getEdastus().get(0).getFault());
		assertNotNull(items.get(0).getEdastus().get(0).getLoetud());

		Edastus edastus = items.get(0).getEdastus().get(1);
		assertEquals("raamatupidamine.30000001", edastus.getSaaja().getRegnr());
		assertEquals(StatusEnum.FAILED.getClassificatorName(), edastus.getStaatus());
		assertNotNull(edastus.getFault());
		assertNotNull(edastus.getLoetud());
		assertEquals("Fault code", edastus.getFault().getFaultcode());
		assertEquals("Fault string", edastus.getFault().getFaultstring());

	}

	// TODO: send multiple containers
	@Test
	public void organisations() {
		Iterable<Organisation> orgs = organisationRepository.findAll();
		Iterator<Organisation> iterator = orgs.iterator();
		Organisation org = iterator.next();
		// TODO: immitate add and remove of the organisations/representees
		assertEquals("70006317", org.getRegistrationCode());
		assertEquals("DHX.dvk", org.getSubSystem());

		org = iterator.next();
		org = iterator.next();
		org = iterator.next();
		org = iterator.next();
		org = iterator.next();
		org = iterator.next();
		assertEquals("70000004", org.getRegistrationCode());
		assertEquals("DHX.adit", org.getSubSystem());

		org = iterator.next();
		assertEquals("500", org.getRegistrationCode());
		assertEquals("system", org.getSubSystem());
		assertNotNull(org.getRepresentor());
		assertEquals("30000001", org.getRepresentor().getRegistrationCode());
	}

	/**
	 * Test of getSendingOptions service.
	 * 
	 * @throws IOException
	 * @throws DhxException
	 */
	@Test
	public void getSendingOptions() throws IOException, DhxException {
		Source requestEnvelope = new StreamSource(
				new ClassPathResource(resourceFolder + "getSendingOptions.xml").getFile());
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:getSendingOptionsResponse[1]", getDhlNamespaceMap()).exists());

		ArgumentCaptor<InstitutionArrayType> argument = ArgumentCaptor.forClass(InstitutionArrayType.class);
		Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
		InstitutionArrayType items = argument.getValue();
		assertEquals(10, items.getAsutus().size());

		assertEquals("dhl", items.getAsutus().get(0).getSaatmine().getSaatmisviis().get(0));

		// organisation with subsustem
		assertEquals("dvk.70006317", items.getAsutus().get(0).getRegnr());
		assertEquals("Riigi Infosüsteemi Amet", items.getAsutus().get(0).getNimi());

		// regular organisation
		assertEquals("30000001", items.getAsutus().get(1).getRegnr());
		assertEquals("Hõbekuuli OÜ", items.getAsutus().get(1).getNimi());

		// organisation with subsystem
		assertEquals("raamatupidamine.30000001", items.getAsutus().get(2).getRegnr());
		assertEquals("Hõbekuuli OÜ", items.getAsutus().get(2).getNimi());

		// regular organisation
		assertEquals("40000001", items.getAsutus().get(3).getRegnr());
		assertEquals("Ministeerium X", items.getAsutus().get(3).getNimi());

		// organisation with non standard subsystem(without dot(.) iafter
		// prefix)
		assertEquals("DHXsubsystem.40000001", items.getAsutus().get(4).getRegnr());
		assertEquals("Ministeerium X", items.getAsutus().get(4).getNimi());

		// regular organisation
		assertEquals("70000004", items.getAsutus().get(5).getRegnr());
		assertEquals("Asutus Y", items.getAsutus().get(5).getNimi());

		// organisation with special subsystem
		assertEquals("adit", items.getAsutus().get(6).getRegnr());
		assertEquals("Asutus Y", items.getAsutus().get(6).getNimi());

		// representee with subsystem
		assertEquals("system.500", items.getAsutus().get(7).getRegnr());
		assertEquals("Representee 1", items.getAsutus().get(7).getNimi());

		// representee with special subsystem
		assertEquals("rt", items.getAsutus().get(8).getRegnr());
		assertEquals("Representee 2", items.getAsutus().get(8).getNimi());

		// regular representee
		assertEquals("500", items.getAsutus().get(9).getRegnr());
		assertEquals("Representee 3", items.getAsutus().get(9).getNimi());
	}

	private SendDocument getSendDocumentRequest(DataHandler handler, String consignmentId, String recipient,
			String recipientSystem) throws DhxException {
		SendDocument request = new SendDocument();
		request.setConsignmentId(consignmentId);
		request.setDHXVersion("1.0");
		request.setRecipient(recipient);
		request.setRecipientSystem(recipientSystem);
		request.setDocumentAttachment(handler);
		return request;
	}

	/**
	 * Tests whole flow. Starts with the document being sent from
	 * DHX(sendDocument service). Then the document will be
	 * received(receiveDocuments) and marked as received(markDocumentsReceived).
	 * 
	 * @throws DhxException
	 */
	@Test
	public void sendDocumentNormal() throws DhxException {

		// receive documents. check that there are no documents to receive before document from DHX is sent
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		ReceiveDocuments receiveDocumentsRequest = new ReceiveDocuments();
		receiveDocumentsRequest.setKeha(new ReceiveDocumentsV4RequestType());
		XRoadClientIdentifierType receiveClient = IntegrationTestHelper.getClient("40000001");
		XRoadServiceIdentifierType receiveService = IntegrationTestHelper.getService("40000001", "v4");
		Source receiveDocumentsEnvelope = IntegrationTestHelper.getEnvelope(receiveClient, receiveService, null,
				receiveDocumentsRequest);
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(receiveDocumentsEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:receiveDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		ArgumentCaptor<List> getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		List<DecContainer> items = getSendStatusArgument.getValue();
		assertEquals(0, items.size());

		
		
		//send document
		DecContainer cont = getContainer("30000001", "40000001", "30000001");
		cont.setDecMetadata(new DecContainer.DecMetadata());
		cont.getDecMetadata().setDecId(BigInteger.valueOf(9999));
		cont.getDecMetadata().setDecFolder("/");
		cont.getDecMetadata().setDecReceiptDate(ConversionUtil.toGregorianCalendar(new Date()));
		File file = dhxMarshallerService.marshall(cont);
		DataHandler handler = new DataHandler(new FileDataSource(file));
		SendDocument request = getSendDocumentRequest(handler, "consignment1", null, null);
		// reset because we just did a request into that service.
		Mockito.reset(convertationService);
		Mockito.doReturn(request).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		XRoadClientIdentifierType client = IntegrationTestHelper.getClient("30000001");
		XRoadServiceIdentifierType service = IntegrationTestHelper.getService("40000001", "v4");

		Source envelope = IntegrationTestHelper.getEnvelope(client, service, null, new SendDocument());
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(envelope)).andExpect(ResponseMatchers
				.xpath("//ns9:sendDocumentResponse[1]/ns9:receiptId[1]", getDhxNamespaceMap()).exists());
		
		//just to check that the document wont be sent to DHX
		soapService.sendDocumentsToDhx();

		// receive documents
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		receiveDocumentsRequest = new ReceiveDocuments();
		receiveDocumentsRequest.setKeha(new ReceiveDocumentsV4RequestType());
		receiveDocumentsEnvelope = IntegrationTestHelper.getEnvelope(receiveClient, receiveService, null,
				receiveDocumentsRequest);
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(receiveDocumentsEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:receiveDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		items = getSendStatusArgument.getValue();
		assertEquals(1, items.size());
		assertEquals("30000001", items.get(0).getTransport().getDecSender().getOrganisationCode());
		assertEquals("40000001", items.get(0).getTransport().getDecRecipient().get(0).getOrganisationCode());
		assertNotEquals(BigInteger.valueOf(9999), items.get(0).getDecMetadata().getDecId());
		BigInteger docId = items.get(0).getDecMetadata().getDecId();

		// mark documents received
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		MarkDocumentsReceived markDocumentsReceived = new MarkDocumentsReceived();
		markDocumentsReceived.setKeha(new MarkDocumentsReceivedV3RequestType());
		List<TagasisideType> tagasisides = new ArrayList<TagasisideType>();
		TagasisideType tagasiside = new TagasisideType();
		tagasiside.setDhlId(docId);
		tagasiside.setVastuvotjaStaatusId(BigInteger.valueOf(RecipientStatusEnum.ACCEPTED.getClassificatorId()));
		tagasisides.add(tagasiside);
		markDocumentsReceived.getKeha().setDokumendid(tagasisides);
		receiveService.setServiceVersion("v3");
		Source markDocumentsReceivedEnvelope = IntegrationTestHelper.getEnvelope(receiveClient, receiveService, null,
				markDocumentsReceived);
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(markDocumentsReceivedEnvelope)).andExpect(
				ResponseMatchers.xpath("//ns4:markDocumentsReceivedResponse[1]/keha[1]", getDhlNamespaceMap())
						.evaluatesTo("OK"));

		// receive documents
		Mockito.reset(convertationService);
		Mockito.doCallRealMethod().when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));
		receiveDocumentsRequest = new ReceiveDocuments();
		receiveDocumentsRequest.setKeha(new ReceiveDocumentsV4RequestType());
		receiveService.setServiceVersion("v4");
		receiveDocumentsEnvelope = IntegrationTestHelper.getEnvelope(receiveClient, receiveService, null,
				receiveDocumentsRequest);
		mockClient.sendRequest(RequestCreators.withSoapEnvelope(receiveDocumentsEnvelope))
				.andExpect(ResponseMatchers.xpath("//ns4:receiveDocumentsResponse[1]", getDhlNamespaceMap()).exists());
		getSendStatusArgument = ArgumentCaptor.forClass(List.class);
		Mockito.verify(convertationService).createDatahandlerFromList(getSendStatusArgument.capture());
		items = getSendStatusArgument.getValue();
		assertEquals(0, items.size());

		file.delete();
	}

}

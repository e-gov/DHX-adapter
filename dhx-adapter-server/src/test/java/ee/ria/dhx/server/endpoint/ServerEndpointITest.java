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
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.service.ConvertationService;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.Base64BinaryType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.DocumentsArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.InstitutionArrayType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4ResponseTypeUnencoded;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.AccessConditionType;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.OrganisationType;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.representation.XRoadRepresentedPartyType;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
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

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
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

	private SendDocuments getSendDocumentRequest(DecContainer... containers) throws DhxException{
		SendDocuments request = new SendDocuments();
		request.setKeha(new SendDocumentsV4RequestType());
		request.getKeha().setDokumendid(new Base64BinaryType());
		try{
		log.debug("containers: " + WsUtil.readInput(getSendDocumentsAttachment(containers).getInputStream()));
		}catch(IOException ex) {
			log.error(ex.getMessage(), ex);
		}
		request.getKeha().getDokumendid().setHref(getSendDocumentsAttachment(containers));
		return request;
	}

	private DecContainer getContainer(String senderOrganisationId, String... recipientOrganisationIds) throws DhxException{
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

	@Test
	public void sendDocumentsTry() throws DhxException {
		DecContainer cont = getContainer("30000001", "40000001");
		SendDocuments request = getSendDocumentRequest(cont);
		//reset because we just did a request into that service. 
		Mockito.reset(convertationService);
		Mockito.doReturn(request).when(marshaller).unmarshal(any(Source.class), any(MimeContainer.class));

		XRoadClientIdentifierType client = IntegrationTestHelper.getClient("30000001");
		XRoadServiceIdentifierType service = IntegrationTestHelper.getService("40000001", "v4");
		//XRoadRepresentedPartyType representee = IntegrationTestHelper.getRepresentee("");
		Source envelope = IntegrationTestHelper.getEnvelope(client, service, null, new SendDocuments());

		mockClient.sendRequest(RequestCreators.withSoapEnvelope(envelope))
				.andExpect(ResponseMatchers.xpath("//ns4:sendDocumentsResponse[1]", getDhlNamespaceMap()).exists());

		ArgumentCaptor<SendDocumentsV4ResponseTypeUnencoded.Keha> argument = ArgumentCaptor
				.forClass(SendDocumentsV4ResponseTypeUnencoded.Keha.class);
		Mockito.verify(convertationService).createDatahandlerFromObject(argument.capture());
		SendDocumentsV4ResponseTypeUnencoded.Keha keha = argument.getValue();

		assertEquals(1, keha.getDhlId().size());
	}
	
	@Test
	public void organisations () {
		Iterable<Organisation> orgs = organisationRepository.findAll();
		Organisation org =  orgs.iterator().next();
		//TODO: immitate add and remove of the organisations/representees
		assertEquals("70006317", org.getRegistrationCode());
		assertEquals("DHX.dvk", org.getSubSystem());
	}

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

}

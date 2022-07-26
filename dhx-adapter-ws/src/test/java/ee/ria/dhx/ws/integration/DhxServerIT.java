package ee.ria.dhx.ws.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.server.RequestCreators.withSoapEnvelope;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.mock.MockWebServiceClient;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.ws.TestApp;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.endpoint.DhxEndpoint;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.ws.test.server.ResponseMatchers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * Tests on DhxEndpoint. Real XML-s are sent to endpoint and received response is being validated.
 * 
 * @author Aleksei Kokarev
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:test-application.properties")
@ContextConfiguration(classes = TestApp.class)
// @WebIntegrationTest("server.port:9000")
public class DhxServerIT {

  @Autowired
  private ApplicationContext applicationContext;

  private MockWebServiceClient mockClient;

  @Autowired
  DhxEndpoint endpoint;

  @Autowired
  Jaxb2Marshaller marshaller;

  @Autowired
  DhxImplementationSpecificService specificService;

  @Autowired
  SoapConfig soapConfig;


  String resourceFolder = "endpoint/";

  /*
   * Unmarshaller unmarshaller;
   * 
   * JAXBContext context;
   */

  @Before
  public void init() throws DhxException {
    mockClient = MockWebServiceClient.createClient(applicationContext);
    List<InternalXroadMember> members = createMemberList();
    addRegularMember(members);
    when(specificService.getAdresseeList()).thenReturn(members);
    when(
        specificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("receiptId");
    when(specificService.isDuplicatePackage(any(InternalXroadMember.class), Mockito.anyString()))
        .thenReturn(false);
    List<String> acceptedSubsystems = new ArrayList<String>();
    acceptedSubsystems.add("DHX");
    soapConfig.setAcceptedSubsystemsAsList(acceptedSubsystems);
    when(specificService.getRepresentationList()).thenReturn(null);
    Mockito.doCallRealMethod().when(marshaller)
        .unmarshal(any(Source.class), any(MimeContainer.class));
  }

  // method creates source, and mock the request of the marshaller, because we need to set
  // attachment aswell, but mock client does not support that
  private Source createSource(String fileName, String capsuleFileName) throws Exception {
    Source requestEnvelope =
        new StreamSource(new ClassPathResource(resourceFolder + fileName).getFile());
    Source requestPayload =
        new StreamSource(new ClassPathResource(resourceFolder + "payload_" + fileName).getFile());
    // first unmarshall as the unmarshaller should, then set attachment manually
    SendDocument request = (SendDocument) marshaller.unmarshal(requestPayload);
    if (capsuleFileName != null) {
      FileDataSource source =
          new FileDataSource(new ClassPathResource(resourceFolder + capsuleFileName).getFile());
      DataHandler handler = new DataHandler(source);
      request.setDocumentAttachment(handler);
    } else {
      request.setDocumentAttachment(null);
    }
    // need to return class variable, because only first mock work. therefore mock always will
    // return object variable and we will change it
    Mockito.doReturn(request).when(marshaller)
        .unmarshal(any(Source.class), any(MimeContainer.class));
    return requestEnvelope;
  }


  private Map<String, String> getDhxNamespaceMap() {
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("ns3", "http://dhx.x-road.eu/producer");
    return namespaces;
  }

  private List<InternalXroadMember> createMemberList() throws DhxException {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    return members;
  }

  private void addRegularMember(List<InternalXroadMember> members) {
    InternalXroadMember member =
        new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", null);
    members.add(member);
  }

  @Test
  public void sendDocumentSuccess() throws Exception {
    Source requestEnvelope = createSource("sendDocument_request.xml", "kapsel_21.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:receiptId[1]",
                getDhxNamespaceMap())
                .evaluatesTo("receiptId"));

  }

  @Test
  public void sendDocumentWrongCapsule() throws Exception {
    Source requestEnvelope = createSource("sendDocument_request.xml", "kapsel_21_wrong.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.Validation"));

  }

  @Test
  public void sendDocumentNotCapsule() throws Exception {
    Source requestEnvelope = createSource("sendDocument_request.xml", "kapsel_21_not_kapsel.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.Validation"));

  }

  @Test
  public void sendDocumentNoCapsule() throws Exception {
    Source requestEnvelope = createSource("sendDocument_request.xml", null);
    mockClient
        .sendRequest(withSoapEnvelope(requestEnvelope))
        .
        andExpect(
            ResponseMatchers
                .serverOrReceiverFault("DHXException code: EXCTRACTION_ERROR Attached capsule is not found in request"));

  }

  @Test
  public void sendDocumentWrongRecipient() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request.xml", "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.InvalidAddressee"));

  }

  @Test
  public void sendDocumentWrongSender() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request.xml", "kapsel_21_other_sender.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.SenderDoesNotMatch"));

  }

  @Test
  public void sendDocumentDuplicate() throws Exception {
    when(specificService.isDuplicatePackage(any(InternalXroadMember.class), Mockito.anyString()))
        .thenReturn(true);
    Source requestEnvelope =
        createSource("sendDocument_request.xml", "kapsel_21_other_sender.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.Duplicate"));

  }

  @Test
  public void sendDocumentWrongSubsystem() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request_subsystem.xml", "kapsel_21.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.InvalidAddressee"));

  }

  @Test
  public void sendDocumentSubsystem() throws Exception {
    List<String> acceptedSubsystems = new ArrayList<String>();
    acceptedSubsystems.add("DHX");
    acceptedSubsystems.add("DHX.system");
    soapConfig.setAcceptedSubsystemsAsList(acceptedSubsystems);
    Source requestEnvelope =
        createSource("sendDocument_request_subsystem.xml", "kapsel_21.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:receiptId[1]",
                getDhxNamespaceMap())
                .evaluatesTo("receiptId"));

  }

  @Test
  public void sendDocumentWrongVersion() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request_version.xml", "kapsel_21.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.UnsupportedVersion"));

  }

  @Test
  public void sendDocumentWrongRepresentee() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request_representee.xml", "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.InvalidAddressee"));

  }

  @Test
  public void sendDocumentOutdatedRepresentee() throws Exception {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee =
        new DhxRepresentee("500", new Date(), new Date(), "Representee 1", null);
    representees.add(representee);
    Source requestEnvelope =
        createSource("sendDocument_request_representee.xml", "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.InvalidAddressee"));

  }

  @Test
  public void sendDocumentOutdatedAndValidRepresentee() throws Exception {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee =
        new DhxRepresentee("500", new Date(), new Date(), "Representee 1", null);
    representees.add(representee);
    representee = new DhxRepresentee("500", new Date(), null, "Representee 1", null);
    representees.add(representee);
    when(specificService.getRepresentationList()).thenReturn(representees);
    Source requestEnvelope =
        createSource("sendDocument_request_representee.xml", "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:receiptId[1]",
                getDhxNamespaceMap())
                .evaluatesTo("receiptId"));

  }

  @Test
  public void sendDocumentRepresentee() throws Exception {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee =
        new DhxRepresentee("500", new Date(), null, "Representee 1", null);
    representees.add(representee);
    when(specificService.getRepresentationList()).thenReturn(representees);
    Source requestEnvelope =
        createSource("sendDocument_request_representee.xml", "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:receiptId[1]",
                getDhxNamespaceMap())
                .evaluatesTo("receiptId"));

  }

  @Test
  public void sendDocumentWrongRepresenteeSubsystem() throws Exception {
    Source requestEnvelope =
        createSource("sendDocument_request_representee_subsystem.xml",
            "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:fault[1]/ns3:faultCode[1]",
                getDhxNamespaceMap())
                .evaluatesTo("DHX.InvalidAddressee"));

  }

  @Test
  public void sendDocumentRepresenteeSubsystem() throws Exception {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee =
        new DhxRepresentee("500", new Date(), null, "Representee 1", "system");
    representees.add(representee);
    when(specificService.getRepresentationList()).thenReturn(representees);
    Source requestEnvelope =
        createSource("sendDocument_request_representee_subsystem.xml",
            "kapsel_21_other_adressee.xml");
    mockClient.sendRequest(withSoapEnvelope(requestEnvelope)).
        andExpect(
            ResponseMatchers.xpath("//ns3:sendDocumentResponse[1]/ns3:receiptId[1]",
                getDhxNamespaceMap())
                .evaluatesTo("receiptId"));

  }

  @Test
  public void getRepresentationList() throws Exception {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    Date date = new Date();
    DhxRepresentee representee =
        new DhxRepresentee("500", date, null, "Representee 1", "system");
    representees.add(representee);
    representee = new DhxRepresentee("500", date, date, "Representee 1", null);
    representees.add(representee);
    when(specificService.getRepresentationList()).thenReturn(representees);
    Source requestEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "representationList.xml").getFile());
    mockClient
        .sendRequest(withSoapEnvelope(requestEnvelope))
        .
        andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]/ns3:memberCode[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("500"))
        .andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]/ns3:representeeName[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("Representee 1"))
        .andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]/ns3:representeeSystem[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("system"))
        .andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]/ns3:endDate[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]/ns3:startDate[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[2]/ns3:endDate[1]",
                    getDhxNamespaceMap())
                .exists());
  }


  @Test
  public void getRepresentationListEmpty() throws Exception {
    Source requestEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "representationList.xml").getFile());
    mockClient
        .sendRequest(withSoapEnvelope(requestEnvelope))
        .
        andExpect(
            ResponseMatchers
                .xpath(
                    "//ns3:representationListResponse[1]/ns3:representees[1]/ns3:representee[1]",
                    getDhxNamespaceMap())
                .doesNotExist());
  }
}

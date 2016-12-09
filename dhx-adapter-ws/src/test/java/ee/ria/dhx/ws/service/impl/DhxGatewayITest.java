package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.ws.TestApp;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.endpoint.DhxEndpoint;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.ws.service.DhxPackageService;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource("classpath:test-application.properties")
@ContextConfiguration(classes = TestApp.class)
public class DhxGatewayITest {


  private MockWebServiceServer mockServer;

  @Autowired
  DhxPackageProviderService dhxPackageProviderService;

  @Autowired
  DhxPackageService dhxPackageService;

  @Autowired
  private ApplicationContext applicationContext;


  @Autowired
  DhxEndpoint endpoint;

  @Autowired
  Jaxb2Marshaller marshaller;

  @Autowired
  DhxImplementationSpecificService specificService;

  @Autowired
  SoapConfig soapConfig;

  @Autowired
  AddressService addressService;


  String resourceFolder = "endpoint/";

  @Before
  public void init() throws Exception {
    mockServer = MockWebServiceServer.createServer(applicationContext);
    List<InternalXroadMember> members = createMemberList();
    addMembers(members);
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

  private List<InternalXroadMember> createMemberList() throws DhxException {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    return members;
  }

  private void addMembers(List<InternalXroadMember> members) {
    InternalXroadMember member =
        new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", null);
    members.add(member);
    DhxRepresentee representee = new DhxRepresentee("500", new Date(), null, "Name", null);
    member =
        new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", representee);
    members.add(member);
    representee = new DhxRepresentee("500", new Date(), null, "Name", "system");
    member =
        new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", representee);
    members.add(member);

  }

  private Map<String, String> getDhxNamespaceMap() {
    Map<String, String> namespaces = new HashMap<String, String>();
    namespaces.put("ns3", "http://dhx.x-road.eu/producer");
    return namespaces;
  }

  @Test
  public void sendDocumentSoapFault() throws Exception {
    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "sendDocument_response_soap_fault.xml")
                .getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:sendDocument[1]/ns3:consignmentId[1]",
                getDhxNamespaceMap())
            .evaluatesTo("consignmentId"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:DHXVersion[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("1.0"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:documentAttachment[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipient[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipientSystem[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andRespond(withSoapEnvelope(responseEnvelope));
    File capsuleFile = new ClassPathResource(resourceFolder + "kapsel_21.xml").getFile();
    InternalXroadMember recipient = addressService.getClientForMemberCode("400", null);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(capsuleFile, "consignmentId", recipient);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals("Server", result.getResponse().getFault().getFaultCode());
    mockServer.verify();
  }

  @Test
  public void sendDocumentFault() throws Exception {
    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "sendDocument_response_fault.xml").getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:sendDocument[1]/ns3:consignmentId[1]",
                getDhxNamespaceMap())
            .evaluatesTo("consignmentId"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:DHXVersion[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("1.0"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:documentAttachment[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipient[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipientSystem[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andRespond(withSoapEnvelope(responseEnvelope));
    File capsuleFile = new ClassPathResource(resourceFolder + "kapsel_21.xml").getFile();
    InternalXroadMember recipient = addressService.getClientForMemberCode("400", null);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(capsuleFile, "consignmentId", recipient);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals("DHX.InvalidAddressee", result.getResponse().getFault().getFaultCode());
    mockServer.verify();
  }

  @Test
  public void sendDocument() throws Exception {
    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "sendDocument_response.xml").getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:sendDocument[1]/ns3:consignmentId[1]",
                getDhxNamespaceMap())
            .evaluatesTo("consignmentId"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:DHXVersion[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("1.0"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:documentAttachment[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipient[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipientSystem[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andRespond(withSoapEnvelope(responseEnvelope));
    File capsuleFile = new ClassPathResource(resourceFolder + "kapsel_21.xml").getFile();
    InternalXroadMember recipient = addressService.getClientForMemberCode("400", null);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(capsuleFile, "consignmentId", recipient);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals("12423", result.getResponse().getReceiptId());
    assertNull(result.getResponse().getFault());
    assertNull(result.getOccuredException());
    mockServer.verify();
  }


  @Test
  public void sendDocumentRepresentee() throws Exception {
    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "sendDocument_response.xml").getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:sendDocument[1]/ns3:consignmentId[1]",
                getDhxNamespaceMap())
            .evaluatesTo("consignmentId"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:DHXVersion[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("1.0"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:documentAttachment[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipient[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("500"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipientSystem[1]",
                    getDhxNamespaceMap())
                .doesNotExist())
        .andRespond(withSoapEnvelope(responseEnvelope));
    File capsuleFile = new ClassPathResource(resourceFolder + "kapsel_21.xml").getFile();
    InternalXroadMember recipient = addressService.getClientForMemberCode("500", null);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(capsuleFile, "consignmentId", recipient);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals("12423", result.getResponse().getReceiptId());
    assertNull(result.getResponse().getFault());
    assertNull(result.getOccuredException());
    mockServer.verify();
  }

  @Test
  public void sendDocumentRepresenteeSubsystem() throws Exception {
    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "sendDocument_response.xml").getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:sendDocument[1]/ns3:consignmentId[1]",
                getDhxNamespaceMap())
            .evaluatesTo("consignmentId"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:DHXVersion[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("1.0"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:documentAttachment[1]",
                    getDhxNamespaceMap())
                .exists())
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipient[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("500"))
        .andExpect(
            RequestMatchers
                .xpath(
                    "//ns3:sendDocument[1]/ns3:recipientSystem[1]",
                    getDhxNamespaceMap())
                .evaluatesTo("system"))
        .andRespond(withSoapEnvelope(responseEnvelope));
    File capsuleFile = new ClassPathResource(resourceFolder + "kapsel_21.xml").getFile();
    InternalXroadMember recipient = addressService.getClientForMemberCode("500", "system");
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(capsuleFile, "consignmentId", recipient);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals("12423", result.getResponse().getReceiptId());
    assertNull(result.getResponse().getFault());
    assertNull(result.getOccuredException());
    mockServer.verify();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void getAddresseeList() throws DhxException, IOException {
    when(specificService.getAdresseeList()).thenReturn(null);
    InputStream stream =
        new FileInputStream(new ClassPathResource("shared-params.xml").getFile());
    AddressServiceImpl addressServiceImpl = (AddressServiceImpl) addressService;
    Mockito.doReturn(stream).when(addressServiceImpl).getGlobalConfStream();
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    when(specificService.getAdresseeList()).thenReturn(new ArrayList<InternalXroadMember>());

    Source responseEnvelope =
        new StreamSource(
            new ClassPathResource(resourceFolder + "representationList_response.xml").getFile());
    mockServer.expect(
        RequestMatchers
            .xpath(
                "//ns3:representationList[1]",
                getDhxNamespaceMap())
            .exists())
        .andRespond(withSoapEnvelope(responseEnvelope));
    // here will be returned list that was given from mock. the real list will be captured using
    // ArgumentCaptor
    List<InternalXroadMember> listRet = addressService.getAdresseeList();
    assertEquals(0, listRet.size());
    Mockito.verify(specificService).saveAddresseeList(argument.capture());
    List<InternalXroadMember> list = argument.getValue();
    for (InternalXroadMember member : list) {
      log.debug("got member: " + member.toString());
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    assertEquals(9, list.size());
    assertEquals("ee-dev", list.get(0).getXroadInstance());
    assertEquals("GOV", list.get(0).getMemberClass());
    assertEquals("70006317", list.get(0).getMemberCode());
    assertEquals("DHX.dvk", list.get(0).getSubsystemCode());
    assertEquals("30000001", list.get(7).getMemberCode());
    assertEquals("COM", list.get(7).getMemberClass());
    assertEquals("500", list.get(7).getRepresentee().getRepresenteeCode());
    assertEquals("system", list.get(7).getRepresentee().getRepresenteeSystem());
    assertEquals("Representee 1", list.get(7).getRepresentee().getRepresenteeName());
    assertEquals("09.12.2016 19:10", sdf.format(list.get(7).getRepresentee().getStartDate()));
    assertNull(list.get(7).getRepresentee().getEndDate());
    assertNull(list.get(8).getRepresentee().getRepresenteeSystem());
    assertEquals("09.12.2016 19:10", sdf.format(list.get(8).getRepresentee().getEndDate()));
    mockServer.verify();
    specificService.saveAddresseeList(null);
  }
}

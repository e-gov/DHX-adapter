package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecSender;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileDataHandler;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.ws.context.MessageContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class DhxPackageServceImplTest {

  private static final String FILE_NAME_KAPSEL_21_XML = "kapsel_21.xml";
  private FileDataHandler FILE_HANDLER_KAPSEL_21_XML = FileUtil.getDatahandlerFromFile(
          ResourceUtils.getFile("classpath:" + FILE_NAME_KAPSEL_21_XML)
  );

  private final SendDocument REQUEST = new SendDocument() {{
    consignmentId = "consignmentId";
    documentAttachment = FILE_HANDLER_KAPSEL_21_XML;
    dhxVersion = "1.0";
  }};
  private final InternalXroadMember CLIENT = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
  private final InternalXroadMember SERVICE = new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
  private final InternalXroadMember SERVICE_SUBSYSTEM = new InternalXroadMember("ee", "GOV", "401", "DHX.subsystem", "Name", null);

  @Mock
  private DhxGateway dhxGateway;

  @Mock
  private DhxImplementationSpecificService dhxImplementationSpecificService;

  @Mock
  private DhxConfig dhxConfig;

  @Mock
  private DhxMarshallerService dhxMarshallerService;

  @Mock
  private CapsuleConfig capsuleConfig;

  @Mock
  private SoapConfig soapConfig;

  @InjectMocks
  private DhxOrganisationFactory dhxOrganisationFactory = spy(DhxOrganisationFactory.class);

  @InjectMocks
  private DhxPackageServiceImpl dhxPackageService;


  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  public DhxPackageServceImplTest() throws FileNotFoundException {}

  @Before
  public void init() throws DhxException {
    when(dhxConfig.getCapsuleValidate()).thenReturn(true);
    when(dhxConfig.getCheckDhxVersion()).thenReturn(true);
    when(dhxConfig.getCheckDuplicate()).thenReturn(true);
    when(dhxConfig.getCheckRecipient()).thenReturn(true);
    when(dhxConfig.getCheckSender()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getAcceptedDhxProtocolVersions()).thenReturn(",1.0,");

    when(capsuleConfig.getCurrentCapsuleVersion()).thenReturn(CapsuleVersionEnum.V21);
    when(capsuleConfig.getXsdForVersion(CapsuleVersionEnum.V21)).thenReturn("jar://Dvk_kapsel_vers_2_1_eng_est.xsd");

    List<String> subsystems = Lists.newArrayList("DHX");
    when(soapConfig.getAcceptedSubsystemsAsList()).thenReturn(subsystems);
    when(soapConfig.getMemberCode()).thenReturn("401");
    when(soapConfig.getDhxSubsystemPrefix()).thenReturn("DHX");

    CapsuleAdressee addressee = new CapsuleAdressee("401");
    List<CapsuleAdressee> addressees = Lists.newArrayList(addressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(addressees);

    CapsuleAdressee sender = new CapsuleAdressee("400");
    when(capsuleConfig.getSenderFromContainer(any())).thenReturn(sender);
    when(dhxImplementationSpecificService.isDuplicatePackage(any(InternalXroadMember.class), anyString()))
        .thenReturn(false);

  }



  @Test
  public void sendPackage() throws DhxException {
    // Prepare
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    SendDocumentResponse resp = new SendDocumentResponse();
    when(dhxGateway.sendDocument(pckg)).thenReturn(resp);
    // Test
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    // Verify
    assertEquals(pckg, result.getSentPackage());
    assertEquals(resp, result.getResponse());
    verify(dhxGateway, times(1)).sendDocument(pckg);
  }

  @Test
  public void sendMultiplePackages() throws DhxException {
    // Prepare
    List<OutgoingDhxPackage> pckgs = new ArrayList<OutgoingDhxPackage>();
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    pckgs.add(pckg);
    pckgs.add(pckg);
    SendDocumentResponse resp = new SendDocumentResponse();
    when(dhxGateway.sendDocument(pckg)).thenReturn(resp);
    // Test
    List<DhxSendDocumentResult> results = dhxPackageService.sendMultiplePackages(pckgs);
    // Verify
    assertEquals(2, results.size());
    assertEquals(pckg, results.get(0).getSentPackage());
    assertEquals(resp, results.get(0).getResponse());
    assertEquals(pckg, results.get(1).getSentPackage());
    assertEquals(resp, results.get(1).getResponse());
    verify(dhxGateway, times(2)).sendDocument(pckg);
  }

  // representee, check recipeint representee
  // check recipient with own representees

  private DecContainer getContainer() {
    DecContainer container = new DecContainer();
    container.setTransport(new DecContainer.Transport());
    DecRecipient recipient = new DecRecipient();
    recipient.setOrganisationCode("401");
    container.getTransport().getDecRecipient().add(recipient);
    DecSender sender = new DecSender();
    sender.setOrganisationCode("400");
    container.getTransport().setDecSender(sender);
    return container;
  }

  @Test
  public void receiveDocumentFromEndpoint() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
            .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
            .thenReturn(new DecContainer());
    // Test
    SendDocumentResponse response = dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1))
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
            .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
            .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresentee() throws DhxException, IOException {
    // Prepare
    REQUEST.setRecipient("410");
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, null);
    representees.add(representee);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("410");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    SendDocumentResponse response = dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1))
        .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeSubsystem() throws DhxException,
      IOException {
    // Prepare
    REQUEST.setRecipient("410");
    REQUEST.setRecipientSystem("subsystem");
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("410");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    SendDocumentResponse response = dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1))
        .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeOutdated() throws DhxException, IOException {
    // Prepare
    REQUEST.setRecipient("410");
    REQUEST.setRecipientSystem("subsystem");
    Date date = new Date();
    Date date2 = new Date(date.getTime()-10000);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", date, date2, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeWrongCapsuleAdressee() throws DhxException,
      IOException {
    // Prepare
    REQUEST.setRecipient("410");
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, null);
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointToWrongRepresentee() throws DhxException, IOException {
    // Prepare
    REQUEST.setRecipient("420");
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointToWrongRepresenteeSubsystem() throws DhxException,
      IOException {
    // Prepare
    REQUEST.setRecipient("410");
    REQUEST.setRecipientSystem("subsystem2");
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointNonDefaultSubsystem() throws DhxException, IOException {
    // Prepare
    List<String> subsystems = Lists.newArrayList("DHX.subsystem");
    when(soapConfig.getAcceptedSubsystemsAsList()).thenReturn(subsystems);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE_SUBSYSTEM, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1))
        .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointWrongSubsystem() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE_SUBSYSTEM, null);
  }


  @Test
  public void receiveDocumentFromEndpointNoParsing() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getParseCapsule()).thenReturn(false);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    verify(dhxMarshallerService, times(1))
        .validate(any(InputStream.class), any(InputStream.class));
    verify(dhxMarshallerService, times(0))
        .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
  }

  @Test
  public void receiveDocumentFromEndpointNovalidation() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getCapsuleValidate()).thenReturn(false);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    verify(dhxMarshallerService, times(1))
        .unmarshallAndValidate(any(InputStream.class), Mockito.isNull(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointNoValidationNoParsing() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getCapsuleValidate()).thenReturn(false);
    when(dhxConfig.getParseCapsule()).thenReturn(false);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    verify(dhxMarshallerService, times(0))
        .unmarshallAndValidate(any(InputStream.class), any(InputStream.class));
    verify(dhxMarshallerService, times(0))
        .validate(any(InputStream.class), Mockito.isNull(InputStream.class));
    verify(dhxImplementationSpecificService, times(1))
        .receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointDuplicate() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    when(dhxImplementationSpecificService.isDuplicatePackage(any(InternalXroadMember.class), anyString()))
        .thenReturn(true);
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.Duplicate Already got package with this consignmentID");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    verify(dhxImplementationSpecificService, times(1))
        .isDuplicatePackage(any(InternalXroadMember.class), anyString());
  }


  @Test
  public void receiveDocumentFromEndpointRepresenteeSender() throws DhxException, IOException {
    // Prepare
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    DhxRepresentee representee = new DhxRepresentee("400", null, null, null, null);
    representee.setRepresenteeCode("400");
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }


  @Test
  public void receiveDocumentFromEndpointWrongCapsuleAdressee() throws DhxException, IOException {
    // Prepare
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointWrongAdressee() throws DhxException, IOException {
    InternalXroadMember service = new InternalXroadMember("ee", "GOV", "501", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoRecipientCheck() throws DhxException, IOException {
    // Prepare
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxConfig.getCheckRecipient()).thenReturn(false);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Test
    SendDocumentResponse response = dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
  }

  @Test
  public void receiveDocumentFromEndpointWrongSender() throws DhxException, IOException {
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "500", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.SenderDoesNotMatch Xroad sender not found in capsule");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, client, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoSenderCheck() throws DhxException, IOException {
    // Prepare
    when(dhxConfig.getCheckSender()).thenReturn(false);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    SendDocumentResponse response = dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
    // Verify
    assertEquals("id1", response.getReceiptId());
    // Test
    assertNull(response.getFault());
  }

  @Test
  public void receiveDocumentFromEndpointWrongVersion() throws DhxException, IOException {
    // Prepare
    REQUEST.setDHXVersion("2.0");
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.UnsupportedVersion Version not supported");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoDocument() throws DhxException {
    // Prepare
    REQUEST.setDocumentAttachment(null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class), any(MessageContext.class)))
        .thenReturn("id1");
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    // Verify
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Attached capsule is not found in request");
    // Test
    dhxPackageService.receiveDocumentFromEndpoint(REQUEST, CLIENT, SERVICE, null);
  }
}

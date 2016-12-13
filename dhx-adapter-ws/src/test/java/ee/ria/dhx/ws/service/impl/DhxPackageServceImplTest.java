package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DhxPackageServceImplTest {

  @Mock
  DhxGateway dhxGateway;

  @Mock
  DhxImplementationSpecificService dhxImplementationSpecificService;

  @Mock
  DhxConfig dhxConfig;

  DhxPackageServiceImpl dhxPackageService;

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Mock
  CapsuleConfig capsuleConfig;

  @Mock
  SoapConfig soapConfig;


  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    dhxPackageService = new DhxPackageServiceImpl();
    dhxPackageService.setDocumentGateway(dhxGateway);
    dhxPackageService.setDhxImplementationSpecificService(dhxImplementationSpecificService);
    dhxPackageService.setConfig(dhxConfig);
    dhxPackageService.setDhxMarshallerService(dhxMarshallerService);
    dhxPackageService.setCapsuleConfig(capsuleConfig);
    dhxPackageService.setSoapConfig(soapConfig);
    when(dhxConfig.getCapsuleValidate()).thenReturn(true);
    when(dhxConfig.getCheckDhxVersion()).thenReturn(true);
    when(dhxConfig.getCheckDuplicate()).thenReturn(true);
    when(dhxConfig.getCheckRecipient()).thenReturn(true);
    when(dhxConfig.getCheckSender()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getAcceptedDhxProtocolVersions()).thenReturn(",1.0,");
    when(capsuleConfig.getCurrentCapsuleVersion()).thenReturn(CapsuleVersionEnum.V21);
    when(capsuleConfig.getXsdForVersion(CapsuleVersionEnum.V21)).thenReturn(
        "jar://Dvk_kapsel_vers_2_1_eng_est.xsd");
    DhxOrganisationFactory.setDhxSubsystemPrefix("DHX");
    List<String> subsystems = new ArrayList<String>();
    subsystems.add("DHX");
    when(soapConfig.getAcceptedSubsystemsAsList()).thenReturn(subsystems);
    when(soapConfig.getMemberCode()).thenReturn("401");

    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);

    CapsuleAdressee sender = new CapsuleAdressee("400");
    when(capsuleConfig.getSenderFromContainer(any())).thenReturn(sender);
    when(
        dhxImplementationSpecificService.isDuplicatePackage(any(InternalXroadMember.class),
            anyString())).thenReturn(false);

  }



  @Test
  public void sendPackage() throws DhxException {
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    SendDocumentResponse resp = new SendDocumentResponse();
    when(dhxGateway.sendDocument(pckg)).thenReturn(resp);
    DhxSendDocumentResult result = dhxPackageService.sendPackage(pckg);
    assertEquals(pckg, result.getSentPackage());
    assertEquals(resp, result.getResponse());
    verify(dhxGateway, times(1)).sendDocument(pckg);
  }

  @Test
  public void sendMultiplePackages() throws DhxException {
    List<OutgoingDhxPackage> pckgs = new ArrayList<OutgoingDhxPackage>();
    OutgoingDhxPackage pckg = new OutgoingDhxPackage(null, null, null, null, null);
    pckgs.add(pckg);
    pckgs.add(pckg);
    SendDocumentResponse resp = new SendDocumentResponse();
    when(dhxGateway.sendDocument(pckg)).thenReturn(resp);
    List<DhxSendDocumentResult> results = dhxPackageService.sendMultiplePackages(pckgs);
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
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresentee() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("410");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, null);
    representees.add(representee);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("410");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeSubsystem() throws DhxException,
      IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("410");
    request.setRecipientSystem("subsystem");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("410");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeOutdated() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("410");
    request.setRecipientSystem("subsystem");
    Date date = new Date();
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", date, date, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointToRepresenteeWrongCapsuleAdressee() throws DhxException,
      IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("410");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, null);
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointToWrongRepresentee() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("420");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointToWrongRepresenteeSubsystem() throws DhxException,
      IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    request.setRecipient("410");
    request.setRecipientSystem("subsystem2");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("410", new Date(), null, null, "subsystem");
    representees.add(representee);
    when(dhxImplementationSpecificService.getRepresentationList()).thenReturn(representees);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointNonDefaultSubsystem() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX.subsystem", "Name", null);
    DhxOrganisationFactory.setDhxSubsystemPrefix("DHX");
    List<String> subsystems = new ArrayList<String>();
    subsystems.add("DHX.subsystem");
    when(soapConfig.getAcceptedSubsystemsAsList()).thenReturn(subsystems);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointWrongSubsystem() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX.subsystem", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }


  @Test
  public void receiveDocumentFromEndpointNoParsing() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(false);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxMarshallerService, times(0)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
  }

  @Test
  public void receiveDocumentFromEndpointNovalidation() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getCapsuleValidate()).thenReturn(false);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        Mockito.isNull(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointNoValidationNoParsing() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getCapsuleValidate()).thenReturn(false);
    when(dhxConfig.getParseCapsule()).thenReturn(false);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    verify(dhxMarshallerService, times(0)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    verify(dhxMarshallerService, times(0)).validate(any(InputStream.class),
        Mockito.isNull(InputStream.class));
    verify(dhxImplementationSpecificService, times(1)).receiveDocument(
        any(IncomingDhxPackage.class), any(MessageContext.class));
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }

  @Test
  public void receiveDocumentFromEndpointDuplicate() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    when(
        dhxImplementationSpecificService.isDuplicatePackage(any(InternalXroadMember.class),
            anyString())).thenReturn(true);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.Duplicate Already got package with this consignmentID");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    verify(dhxImplementationSpecificService, times(1)).isDuplicatePackage(
        any(InternalXroadMember.class), anyString());
  }


  @Test
  public void receiveDocumentFromEndpointRepresenteeSender() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    DhxRepresentee representee = new DhxRepresentee("400", null, null, null, null);
    representee.setRepresenteeCode("400");
    InternalXroadMember client =
        new InternalXroadMember("ee", "GOV", "420", "DHX", "Name", representee);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }


  @Test
  public void receiveDocumentFromEndpointWrongCapsuleAdressee() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in capsule recipient list");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointWrongAdressee() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "501", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx
        .expectMessage("DHX.InvalidAddressee Recipient not found in representativesList and own member code");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoRecipientCheck() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getCheckRecipient()).thenReturn(false);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
  }

  @Test
  public void receiveDocumentFromEndpointWrongSender() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "500", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.SenderDoesNotMatch Xroad sender not found in capsule");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoSenderCheck() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "500", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getCheckSender()).thenReturn(false);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    SendDocumentResponse response =
        dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
    assertEquals("id1", response.getReceiptId());
    assertNull(response.getFault());
  }

  @Test
  public void receiveDocumentFromEndpointWrongVersion() throws DhxException, IOException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    request.setDHXVersion("2.0");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    request.setDocumentAttachment(FileUtil.getDatahandlerFromFile(file));
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("DHX.UnsupportedVersion Version not supported");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }

  @Test
  public void receiveDocumentFromEndpointNoDocument() throws DhxException {
    SendDocument request = new SendDocument();
    request.setConsignmentId("consignmentId");
    request.setDHXVersion("1.0");
    InternalXroadMember client = new InternalXroadMember("ee", "GOV", "400", "DHX", "Name", null);
    InternalXroadMember service =
        new InternalXroadMember("ee", "GOV", "401", "DHX", "Name", null);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(
        dhxImplementationSpecificService.receiveDocument(any(IncomingDhxPackage.class),
            any(MessageContext.class))).thenReturn("id1");
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Attached capsule is not found in request");
    dhxPackageService.receiveDocumentFromEndpoint(request, client, service, null);
  }
}

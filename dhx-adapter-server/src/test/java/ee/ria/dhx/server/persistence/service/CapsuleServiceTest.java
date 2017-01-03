package ee.ria.dhx.server.persistence.service;

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
import ee.ria.dhx.server.persistence.enumeration.RecipientStatusEnum;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecSender;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class CapsuleServiceTest {

  @Mock
  CapsuleConfig capsuleConfig;

  @Value("${dhx.server.treat-cantainer-as-string}")
  Boolean treatContainerAsString;

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Mock
  OrganisationRepository organisationRepository;

  @Mock
  PersistenceService persistenceService;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Mock
  DhxConfig config;

  CapsuleService capsuleService;

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    capsuleService = new CapsuleService();
    capsuleService.setCapsuleConfig(capsuleConfig);
    capsuleService.setDhxMarshallerService(dhxMarshallerService);
    capsuleService.setOrganisationRepository(organisationRepository);
    capsuleService.setPersistenceService(persistenceService);
    when(capsuleConfig.getCurrentCapsuleVersion()).thenReturn(CapsuleVersionEnum.V21);
    when(capsuleConfig.getXsdForVersion(CapsuleVersionEnum.V21))
        .thenReturn("jar://Dvk_kapsel_vers_2_1_eng_est.xsd");
    DhxOrganisationFactory.setDhxSubsystemPrefix("DHX");
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401", null, null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", null, null);
    when(capsuleConfig.getSenderFromContainer(any())).thenReturn(adressee);
    Folder folder = new Folder();
    folder.setName("folder");
    when(persistenceService.getFolderByNameOrDefaultFolder("/")).thenReturn(folder);
    capsuleService.setConfig(config);
    when(config.getCapsuleValidate()).thenReturn(true);

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
    member = new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", representee);
    members.add(member);
    representee = new DhxRepresentee("500", new Date(), null, "Name", "system");
    member = new InternalXroadMember("ee-dev", "GOV", "400", "DHX", "Name1", representee);
    members.add(member);

  }

  private InternalXroadMember getMember(String memberCode, DhxRepresentee representee) {
    return new InternalXroadMember("ee-dev", "GOV", memberCode, "DHX", "Name1", representee);
  }

  private SendDocument getSendDocument(String recipient, String recipientSystem,
      DataHandler documenAttachment) {
    SendDocument document = new SendDocument();
    document.setConsignmentId("consignmentId");
    document.setDHXVersion("1.0");
    document.setRecipient(null);
    document.setRecipientSystem("null");
    document.setDocumentAttachment(documenAttachment);
    return document;
  }

  private DecContainer getDecContainer(InternalXroadMember client, InternalXroadMember service) {
    DecContainer container = new DecContainer();
    container.setTransport(new DecContainer.Transport());
    DecSender sender = new DecSender();
    sender.setOrganisationCode(client.getMemberCode());
    container.getTransport().setDecSender(sender);
    DecRecipient decRecipient = new DecRecipient();
    decRecipient.setOrganisationCode(service.getMemberCode());
    container.getTransport().getDecRecipient().add(decRecipient);
    container.setDecMetadata(new DecContainer.DecMetadata());
    container.getDecMetadata().setDecFolder("/");
    return container;
  }

  @Test
  public void getDocumentFromIncomingContainer() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(clientOrg);

    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);

    // mock folder
    Folder folder = new Folder();
    folder.setName("folder");
    when(persistenceService.getFolderByNameOrDefaultFolder("/")).thenReturn(folder);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    verify(persistenceService, times(0)).getOrganisationFromInternalXroadMemberAndSave(
        any(InternalXroadMember.class), any(Boolean.class), any(Boolean.class));
    assertEquals("V21", document.getCapsuleVersion());
    assertEquals(folder, document.getFolder());
    assertNotNull(document.getContent());
    assertEquals(clientOrg, document.getOrganisation());
    assertEquals(false, document.getOutgoingDocument());
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals("consignmentId",
        document.getTransports().get(0).getRecipients().get(0).getDhxExternalConsignmentId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getDhxExternalReceiptId());
    assertNull(
        document.getTransports().get(0).getRecipients().get(0).getDhxInternalConsignmentId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getFaultCode());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getLastSendDate());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getMetaxml());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getPersonalcode());
    assertEquals(RecipientStatusEnum.ACCEPTED.getClassificatorId(),
        document.getTransports().get(0).getRecipients().get(0).getRecipientStatusId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getStructuralUnit());
    assertNotNull(document.getTransports().get(0).getRecipients().get(0).getSendingStart());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getSendingEnd());

    
    assertEquals(StatusEnum.IN_PROCESS.getClassificatorId(),
        document.getTransports().get(0).getRecipients().get(0).getStatusId());
    assertEquals(StatusEnum.IN_PROCESS.getClassificatorId(),
        document.getTransports().get(0).getStatusId());
    assertNotNull(document.getTransports().get(0).getSendingStart());
    assertNull(document.getTransports().get(0).getSendingEnd());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromIncomingContainerRepresentee() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        client.getRepresentee().getRepresenteeCode(),
        client.getRepresentee().getRepresenteeSystem())).thenReturn(clientOrg);

    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        service.getRepresentee().getRepresenteeCode(),
        service.getRepresentee().getRepresenteeSystem())).thenReturn(serviceOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromIncomingContainerSenderNotFound() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(null);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);
    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, false, false))
        .thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        false, false);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromIncomingContainerSenderRepresenteeNotFound()
      throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("500", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());

    Organisation clientRepresenteeOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(null);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);
    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, true, false))
        .thenReturn(clientOrg);

    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, false, false))
        .thenReturn(clientRepresenteeOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        true, false);
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        false, false);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientRepresenteeOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromIncomingContainerManyRecipients() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    DecRecipient decRecipient = new DecRecipient();
    decRecipient.setOrganisationCode("random");
    container.getTransport().getDecRecipient().add(decRecipient);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromIncomingContainerPersonalCode() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401", "pcode", null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", "pcode2", null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals("pcode",
        document.getTransports().get(0).getRecipients().get(0).getPersonalcode());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals("pcode2", document.getTransports().get(0).getSenders().get(0).getPersonalCode());
  }

  @Test
  public void getDocumentFromIncomingContainerStructuralUnit() throws DhxException, IOException {
    // init package
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);
    SendDocument sendDocument = getSendDocument(null, null, handler);
    DhxOrganisation recipient = DhxOrganisationFactory.createDhxOrganisation(service);
    IncomingDhxPackage pckg = new IncomingDhxPackage(client, service, sendDocument, recipient);
    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);
    StringWriter writer = new StringWriter();
    writer.write("content");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(service.getMemberCode(),
        service.getSubsystemCode())).thenReturn(serviceOrg);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401", null, "sunit2");
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", null, "sunit1");
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromIncomingContainer(pckg, CapsuleVersionEnum.V21);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals("sunit2",
        document.getTransports().get(0).getRecipients().get(0).getStructuralUnit());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals("sunit1",
        document.getTransports().get(0).getSenders().get(0).getStructuralUnit());
  }

  @Test
  public void getDocumentFromOutgoingContainer() throws DhxException, IOException {
    // init input parameters
    // File file = new
    // ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    // DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    InternalXroadMember service = getMember("401", null);

    // mock organisation
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getMemberCode())).thenReturn(serviceOrg);

    DecContainer container = getDecContainer(client, service);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("400", null, null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // mock folder
    Folder folder = new Folder();
    folder.setName("folder");
    when(persistenceService.getFolderByNameOrDefaultFolder("/")).thenReturn(folder);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("401");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("400", "DHX");
    assertEquals("V21", document.getCapsuleVersion());
    assertEquals(folder, document.getFolder());
    assertEquals("container string", document.getContent());
    assertEquals(clientOrg, document.getOrganisation());
    assertEquals(true, document.getOutgoingDocument());
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertNull(
        document.getTransports().get(0).getRecipients().get(0).getDhxExternalConsignmentId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getDhxExternalReceiptId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getFaultCode());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getLastSendDate());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getMetaxml());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getPersonalcode());
    assertEquals(RecipientStatusEnum.ACCEPTED.getClassificatorId(),
        document.getTransports().get(0).getRecipients().get(0).getRecipientStatusId());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getStructuralUnit());
    assertNotNull(document.getTransports().get(0).getRecipients().get(0).getSendingStart());
    assertNull(document.getTransports().get(0).getRecipients().get(0).getSendingEnd());
    /*
     * assertEquals(1, document.getTransports().get(0).getRecipients().get(0)
     * .getStatusHistory().size()); assertEquals(StatusEnum.IN_PROCESS.getClassificatorId(),
     * document.getTransports().get(0).getRecipients().get(0)
     * .getStatusHistory().get(0).getStatusId());
     * assertEquals(RecipientStatusEnum.ACCEPTED.getClassificatorId(),
     * document.getTransports().get(0).getRecipients().get(0)
     * .getStatusHistory().get(0).getRecipientStatusId());
     * assertNull(document.getTransports().get(0).getRecipients().get(0)
     * .getStatusHistory().get(0).getFaultCode());
     */
    assertEquals(StatusEnum.IN_PROCESS.getClassificatorId(),
        document.getTransports().get(0).getRecipients().get(0).getStatusId());
    assertEquals(StatusEnum.IN_PROCESS.getClassificatorId(),
        document.getTransports().get(0).getStatusId());
    assertNotNull(document.getTransports().get(0).getSendingStart());
    assertNull(document.getTransports().get(0).getSendingEnd());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromOutgoingContainerRepresentee() throws DhxException, IOException {
    // init input parameters
    // File file = new
    // ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    // DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        client.getRepresentee().getRepresenteeCode(),
        client.getRepresentee().getRepresenteeSystem())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", null, null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", null, null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("410", null);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromOutgoingContainerSenderNotFound() throws DhxException, IOException {
    // init input parameters
    // File file = new
    // ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    // DataHandler handler = new DataHandler(new FileDataSource(file));
    InternalXroadMember client = getMember("400", null);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(null);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);

    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, false, false))
        .thenReturn(clientOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", null, null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", null, null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        false, false);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromOutgoingContainerSenderRepresenteeNotFound()
      throws DhxException, IOException {
    // init input parameters
    // File file = new
    // ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    // DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getMemberCode());

    Organisation clientRepresenteeOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(client.getMemberCode(),
        client.getSubsystemCode())).thenReturn(null);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);
    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, true, false))
        .thenReturn(clientOrg);

    when(persistenceService.getOrganisationFromInternalXroadMemberAndSave(client, false, false))
        .thenReturn(clientRepresenteeOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);
    /*
     * when( dhxMarshallerService .unmarshallAndValidate(any(InputStream.class),
     * any(InputStream.class))).thenReturn( container);
     */

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", null, null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("410", null, null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("410")).thenReturn(clientRepresenteeOrg);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);
    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        true, false);
    verify(persistenceService, times(1)).getOrganisationFromInternalXroadMemberAndSave(client,
        false, false);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientRepresenteeOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromOutgoingContainerManyRecipients() throws DhxException, IOException {
    // init input parameters
    File file = new ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        client.getRepresentee().getRepresenteeCode(),
        client.getRepresentee().getRepresenteeSystem())).thenReturn(clientOrg);

    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);

    Organisation secondOrg = new Organisation();
    secondOrg.setRegistrationCode("random");
    when(persistenceService.findOrg("random")).thenReturn(secondOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", null, null);
    addressees.add(adressee);
    adressee = new CapsuleAdressee("random", null, null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    adressee = new CapsuleAdressee("400", null, null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(adressee);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("410", null);
    assertEquals(1, document.getTransports().size());
    assertEquals(2, document.getTransports().get(0).getRecipients().size());
    assertEquals(serviceOrg,
        document.getTransports().get(0).getRecipients().get(0).getOrganisation());
    assertEquals(secondOrg,
        document.getTransports().get(0).getRecipients().get(1).getOrganisation());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals(clientOrg,
        document.getTransports().get(0).getSenders().get(0).getOrganisation());
  }

  @Test
  public void getDocumentFromOutgoingContainerPersonalCode() throws DhxException, IOException {
    // init input parameters
    // File file = new
    // ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    // DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        client.getRepresentee().getRepresenteeCode(),
        client.getRepresentee().getRepresenteeSystem())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", "pcode", null);
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    CapsuleAdressee sender = new CapsuleAdressee("400", "pcode2", null);
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(sender);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("410", null);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals("pcode",
        document.getTransports().get(0).getRecipients().get(0).getPersonalcode());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals("pcode2", document.getTransports().get(0).getSenders().get(0).getPersonalCode());
  }

  @Test
  public void getDocumentFromOutgoingContainerStructuralUnit() throws DhxException, IOException {
    // init input parameters
    File file = new ClassPathResource("kapsel_21_gzip_base64.txt").getFile();
    DataHandler handler = new DataHandler(new FileDataSource(file));
    DhxRepresentee clientRepresentee = new DhxRepresentee("410", null, null, null, null);
    InternalXroadMember client = getMember("400", clientRepresentee);
    DhxRepresentee serviceRepresentee = new DhxRepresentee("500", null, null, null, "system");
    InternalXroadMember service = getMember("401", serviceRepresentee);

    // mock organisations
    Organisation clientOrg = new Organisation();
    clientOrg.setRegistrationCode(client.getRepresentee().getRepresenteeCode());
    when(organisationRepository.findByRegistrationCodeAndSubSystem(
        client.getRepresentee().getRepresenteeCode(),
        client.getRepresentee().getRepresenteeSystem())).thenReturn(clientOrg);
    Organisation serviceOrg = new Organisation();
    serviceOrg.setRegistrationCode(service.getMemberCode());
    when(persistenceService.findOrg(service.getRepresentee().getRepresenteeCode()))
        .thenReturn(serviceOrg);

    // mock container
    DecContainer container = getDecContainer(client, service);
    when(dhxMarshallerService.unmarshall(any(InputStream.class))).thenReturn(container);

    StringWriter writer = new StringWriter();
    writer.write("container string");
    when(dhxMarshallerService.marshallToWriterAndValidate(Mockito.eq(container),
        any(InputStream.class)))
            .thenReturn(writer);

    // mock capsule recipient and sender
    List<CapsuleAdressee> addressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("500", null, "sunit");
    addressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(container)).thenReturn(addressees);
    CapsuleAdressee sender = new CapsuleAdressee("400", null, "sunit2");
    when(capsuleConfig.getSenderFromContainer(container)).thenReturn(sender);
    when(persistenceService.findOrg("400")).thenReturn(clientOrg);

    // method call
    Document document =
        capsuleService.getDocumentFromOutgoingContainer(client, service, container, "/",
            CapsuleVersionEnum.V21);
    verify(persistenceService, times(1)).findOrg("500");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("410", null);
    assertEquals(1, document.getTransports().size());
    assertEquals(1, document.getTransports().get(0).getRecipients().size());
    assertEquals("sunit",
        document.getTransports().get(0).getRecipients().get(0).getStructuralUnit());
    assertEquals(1, document.getTransports().get(0).getSenders().size());
    assertEquals("sunit2",
        document.getTransports().get(0).getSenders().get(0).getStructuralUnit());
  }

  @Test
  public void getContainerFromDocument() throws DhxException, IOException {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    String containerStr = WsUtil.readInput(new FileInputStream(file));
    Document doc = new Document();
    doc.setDocumentId(12L);
    doc.setContent(containerStr);
    DecContainer containerReturn = new DecContainer();
    when(dhxMarshallerService.unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class)))
            .thenReturn(containerReturn);
    DecContainer container = (DecContainer) capsuleService.getContainerFromDocument(doc);
    assertEquals(containerReturn, container);
    assertEquals(BigInteger.valueOf(12), container.getDecMetadata().getDecId());
  }
}

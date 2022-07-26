package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DhxPackageProviderServiceImplTest {


  DhxPackageProviderServiceImpl dhxPackageProviderService;

  @Mock
  SoapConfig soapConfig;

  @Mock
  AddressService addressService;

  @Mock
  CapsuleConfig capsuleConfig;

  @Mock
  DhxConfig dhxConfig;

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    dhxPackageProviderService = new DhxPackageProviderServiceImpl();
    dhxPackageProviderService.setAddressService(addressService);
    dhxPackageProviderService.setCapsuleConfig(capsuleConfig);
    dhxPackageProviderService.setDhxConfig(dhxConfig);
    dhxPackageProviderService.setDhxMarshallerService(dhxMarshallerService);
    dhxPackageProviderService.setSoapConfig(soapConfig);

    when(dhxConfig.getCapsuleValidate()).thenReturn(true);
    when(dhxConfig.getCheckDhxVersion()).thenReturn(true);
    when(dhxConfig.getCheckDuplicate()).thenReturn(true);
    when(dhxConfig.getCheckRecipient()).thenReturn(true);
    when(dhxConfig.getCheckSender()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getParseCapsule()).thenReturn(true);
    when(dhxConfig.getProtocolVersion()).thenReturn("1.0");
    when(dhxConfig.getAcceptedDhxProtocolVersions()).thenReturn(",1.0,");
    when(capsuleConfig.getCurrentCapsuleVersion()).thenReturn(CapsuleVersionEnum.V21);
    when(capsuleConfig.getXsdForVersion(CapsuleVersionEnum.V21)).thenReturn(
        "jar://Dvk_kapsel_vers_2_1_eng_est.xsd");
  }

  private InternalXroadMember getMember() {
    InternalXroadMember member =
        new InternalXroadMember("ee", "gov", "code", "DHX", "Name", null);
    return member;
  }

  @Test
  public void getOutgoingPackageFileStringInternalXroadMember() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            recipient);
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageFileStringInternalXroadMemberInternalXroadMember()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", recipient, sender);
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageFileStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", "recCode", "recSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageInputStreamStringInternalXroadMemberInternalXroadMember()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            recipient, sender);
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageInputStreamStringInternalXroadMemberInternalXroadMemberNovalidation()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(dhxConfig.getCapsuleValidate()).thenReturn(false);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            recipient, sender);
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(0)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }


  @Test
  public void getOutgoingPackageInputStreamStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            "recCode", "recSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }


  @Test
  public void getOutgoingPackageFileStringStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", "recCode", "recSystem",
            "senderSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("senderSystem", pckg.getClient().getSubsystemCode());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageInputStreamStringStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            "recCode", "recSystem", "senderSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("senderSystem", pckg.getClient().getSubsystemCode());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
  }


  @Test
  public void getOutgoingPackageFileStringInternalXroadMemberInternalXroadMemberInputStreamString()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", recipient, sender,
            new InputStream() {
              @Override
              public int read() throws IOException {
                return 0;
              }
            }, "1.1");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.1", pckg.getDhxProtocolVersion());
  }


  @Test
  public void getOutgoingPackageFileStringStringStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    sender.setMemberCode("senderCode");
    sender.setSubsystemCode("senderSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(addressService.getClientForMemberCode("senderCode", "senderSystem")).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", "recCode", "recSystem",
            "senderCode", "senderSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("senderCode", pckg.getClient().getMemberCode());
    assertEquals("senderSystem", pckg.getClient().getSubsystemCode());
  }

  @Test
  public void getOutgoingPackageInputStreamStringInternalXroadMemberInternalXroadMemberInputStreamString()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            recipient, sender, new InputStream() {
              @Override
              public int read() throws IOException {
                return 0;
              }
            }, "1.1");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.1", pckg.getDhxProtocolVersion());
  }

  @Test
  public void getOutgoingPackageInputStreamStringStringStringStringString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    sender.setMemberCode("senderCode");
    sender.setSubsystemCode("senderSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(addressService.getClientForMemberCode("senderCode", "senderSystem")).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            "recCode", "recSystem", "senderCode", "senderSystem");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckg.getDhxProtocolVersion());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("senderCode", pckg.getClient().getMemberCode());
    assertEquals("senderSystem", pckg.getClient().getSubsystemCode());
  }


  @Test
  public void getOutgoingPackageInputStreamStringStringStringStringStringInputStreamString()
      throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    recipient.setMemberCode("recCode");
    recipient.setSubsystemCode("recSystem");
    sender.setMemberCode("senderCode");
    sender.setSubsystemCode("senderSystem");
    when(addressService.getClientForMemberCode("recCode", "recSystem")).thenReturn(recipient);
    when(addressService.getClientForMemberCode("senderCode", "senderSystem")).thenReturn(sender);
    OutgoingDhxPackage pckg =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            "recCode", "recSystem", "senderCode", "senderSystem", new InputStream() {
              @Override
              public int read() throws IOException {
                return 0;
              }
            }, "1.1");
    assertEquals(recipient, pckg.getService());
    assertEquals(sender, pckg.getClient());
    assertEquals("consignment", pckg.getInternalConsignmentId());
    assertNotNull(pckg.getDocumentFile());
    verify(dhxMarshallerService, times(1)).validate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.1", pckg.getDhxProtocolVersion());
    assertEquals("recCode", pckg.getService().getMemberCode());
    assertEquals("recSystem", pckg.getService().getSubsystemCode());
    assertEquals("senderCode", pckg.getClient().getMemberCode());
    assertEquals("senderSystem", pckg.getClient().getSubsystemCode());
  }

  @Test
  public void getOutgoingPackageFileString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(addressService.getClientForMemberCode("401", null)).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxMarshallerService.unmarshallAndValidate(any(File.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    List<OutgoingDhxPackage> pckgs =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment");
    assertEquals(1, pckgs.size());
    assertEquals(recipient, pckgs.get(0).getService());
    assertEquals(sender, pckgs.get(0).getClient());
    assertEquals("consignment", pckgs.get(0).getInternalConsignmentId());
    assertNotNull(pckgs.get(0).getDocumentFile());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(File.class),
        any(InputStream.class));
    assertEquals("1.0", pckgs.get(0).getDhxProtocolVersion());
    assertEquals(CapsuleVersionEnum.V21, pckgs.get(0).getParsedContainerVersion());
  }

  @Test
  public void getOutgoingPackageInpuStreamString() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(addressService.getClientForMemberCode("401", null)).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    List<OutgoingDhxPackage> pckgs =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment");
    assertEquals(1, pckgs.size());
    assertEquals(recipient, pckgs.get(0).getService());
    assertEquals(sender, pckgs.get(0).getClient());
    assertEquals("consignment", pckgs.get(0).getInternalConsignmentId());
    assertNotNull(pckgs.get(0).getDocumentFile());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckgs.get(0).getDhxProtocolVersion());
    assertEquals(CapsuleVersionEnum.V21, pckgs.get(0).getParsedContainerVersion());
  }

  @Test
  public void getOutgoingPackageInpuStreamStringCapsuleVersionEnum() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(addressService.getClientForMemberCode("401", null)).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(
        dhxMarshallerService
            .unmarshallAndValidate(any(InputStream.class), any(InputStream.class))).thenReturn(
        new DecContainer());
    List<OutgoingDhxPackage> pckgs =
        dhxPackageProviderService.getOutgoingPackage(new FileInputStream(file), "consignment",
            CapsuleVersionEnum.V21);
    assertEquals(1, pckgs.size());
    assertEquals(recipient, pckgs.get(0).getService());
    assertEquals(sender, pckgs.get(0).getClient());
    assertEquals("consignment", pckgs.get(0).getInternalConsignmentId());
    assertNotNull(pckgs.get(0).getDocumentFile());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(InputStream.class),
        any(InputStream.class));
    assertEquals("1.0", pckgs.get(0).getDhxProtocolVersion());
    assertEquals(CapsuleVersionEnum.V21, pckgs.get(0).getParsedContainerVersion());
  }

  @Test
  public void getOutgoingPackageFileStringCapsuleVersionEnum() throws Exception {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    InternalXroadMember recipient = getMember();
    InternalXroadMember sender = getMember();
    when(addressService.getClientForMemberCode("401", null)).thenReturn(recipient);
    when(soapConfig.getDefaultClient()).thenReturn(sender);
    List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
    CapsuleAdressee adressee = new CapsuleAdressee("401");
    adressees.add(adressee);
    when(capsuleConfig.getAdresseesFromContainer(any())).thenReturn(adressees);
    when(dhxMarshallerService.unmarshallAndValidate(any(File.class), any(InputStream.class)))
        .thenReturn(new DecContainer());
    List<OutgoingDhxPackage> pckgs =
        dhxPackageProviderService.getOutgoingPackage(file, "consignment", CapsuleVersionEnum.V21);
    assertEquals(1, pckgs.size());
    assertEquals(recipient, pckgs.get(0).getService());
    assertEquals(sender, pckgs.get(0).getClient());
    assertEquals("consignment", pckgs.get(0).getInternalConsignmentId());
    assertNotNull(pckgs.get(0).getDocumentFile());
    verify(dhxMarshallerService, times(1)).unmarshallAndValidate(any(File.class),
        any(InputStream.class));
    assertEquals("1.0", pckgs.get(0).getDhxProtocolVersion());
    assertEquals(CapsuleVersionEnum.V21, pckgs.get(0).getParsedContainerVersion());
  }

}

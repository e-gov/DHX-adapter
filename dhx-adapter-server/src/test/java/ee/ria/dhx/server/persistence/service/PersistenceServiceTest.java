package ee.ria.dhx.server.persistence.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;

public class PersistenceServiceTest {

  @Mock
  AddressService addressService;

  @Mock
  OrganisationRepository organisationRepository;

  @Mock
  FolderRepository folderRepository;

  PersistenceService persistenceService;

  @InjectMocks
  DhxOrganisationFactory dhxOrganisationFactory;

  @Mock
  SoapConfig config;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    persistenceService = new PersistenceService();
    persistenceService.setAddressService(addressService);
    persistenceService.setFolderRepository(folderRepository);
    persistenceService.setOrganisationRepository(organisationRepository);
    persistenceService.setDhxOrganisationFactory(dhxOrganisationFactory);
    Folder folder = new Folder();
    folder.setName("folder");
    persistenceService.setSpecialOrganisations("adit,kovtp,rt,eelnoud");
    persistenceService.setConfig(config);
    when(config.getDhxSubsystemPrefix()).thenReturn("DHX");
  }

  private InternalXroadMember getMember(String memberCode, DhxRepresentee representee) {
    return new InternalXroadMember("ee-dev", "GOV", memberCode, "DHX", "Name1", representee);
  }

  @Test
  public void findOrg() throws DhxException {
    String containerOrganisationId = "member1";
    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    org.setIsActive(true);
    org.setDhxOrganisation(true);
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1))
        .findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(),
        Mockito.anyString());
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.eq("DHX"));
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());

  }

  @Test
  public void findOrgSubsystem() throws DhxException {
    String containerOrganisationId = "system.member1";
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(null);

    InternalXroadMember member = getMember("member1", null);
    member.setSubsystemCode("system");
    when(addressService.getClientForMemberCode("member1", "system")).thenReturn(member);

    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    org.setIsActive(true);
    org.setDhxOrganisation(true);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("member1", "system"))
        .thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1))
        .findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(1)).getClientForMemberCode("member1", "system");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("member1",
        "system");
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());
  }

  @Test
  public void findOrgSpecial() throws DhxException {
    String containerOrganisationId = "adit";
    Organisation org = new Organisation();
    org.setRegistrationCode("adit");
    org.setIsActive(true);
    org.setDhxOrganisation(true);
    when(organisationRepository.findBySubSystem("adit")).thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1)).findBySubSystem("adit");
    verify(organisationRepository, times(0)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(),
        Mockito.anyString());
  }

  @Test
  public void findOrgNotFound() throws DhxException {
    String containerOrganisationId = "member1";
    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(null);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Unable to find member in addressregistry by regsitration code");
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1))
        .findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(),
        Mockito.anyString());
    verify(organisationRepository, times(0)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.notNull(String.class));
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());
  }

  @Test
  public void findOrgNotActive() throws DhxException {
    String containerOrganisationId = "system.member1";
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(null);

    InternalXroadMember member = getMember("member1", null);
    member.setSubsystemCode("system");
    when(addressService.getClientForMemberCode("member1", "system")).thenReturn(member);

    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    org.setIsActive(false);
    org.setDhxOrganisation(true);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("member1", "system"))
        .thenReturn(org);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage(
        "Found organisation is either inactive or not registered as DHX orghanisation.");
    persistenceService.findOrg(containerOrganisationId);
  }

  @Test
  public void findOrgNotDhxOrg() throws DhxException {
    String containerOrganisationId = "system.member1";
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(null);

    InternalXroadMember member = getMember("member1", null);
    member.setSubsystemCode("system");
    when(addressService.getClientForMemberCode("member1", "system")).thenReturn(member);

    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    org.setIsActive(true);
    org.setDhxOrganisation(false);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("member1", "system"))
        .thenReturn(org);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage(
        "Found organisation is either inactive or not registered as DHX orghanisation.");
    persistenceService.findOrg(containerOrganisationId);
  }

  @Test
  public void findOrgSubsystemNotFound() throws DhxException {
    String containerOrganisationId = "system.member1";
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null))
        .thenReturn(null);

    InternalXroadMember member = getMember("member1", null);
    member.setSubsystemCode("system");
    when(addressService.getClientForMemberCode("member1", "system")).thenReturn(null);
    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    when(organisationRepository.findByRegistrationCodeAndSubSystem("member1", "system"))
        .thenReturn(org);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Unable to find member in addressregistry by regsitration code");
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1))
        .findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(1)).getClientForMemberCode("member1", "system");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("member1",
        "system");
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());
  }

  @Test
  public void findOrgSpecialNotFound() throws DhxException {
    String containerOrganisationId = "adit";
    Organisation org = new Organisation();
    org.setRegistrationCode("adit");
    when(organisationRepository.findBySubSystem("adit")).thenReturn(null);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Unable to find organisation using organisation code");
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1)).findBySubSystem("adit");
    verify(organisationRepository, times(0)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(),
        Mockito.anyString());
  }

  @Test
  public void getFolderByNameOrDefaultFolder() {
    String folderName = "folder";
    Folder folder = new Folder();
    when(folderRepository.findByName(folderName)).thenReturn(folder);
    Folder foundFolder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
    verify(folderRepository, times(0)).save(any(Folder.class));
    assertEquals(folder, foundFolder);
  }

  @Test
  public void getFolderByNameOrDefaultFolderNotFound() {
    String folderName = "folder";
    when(folderRepository.findByName(folderName)).thenReturn(null);
    Folder foundFolder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
    verify(folderRepository, times(1)).save(any(Folder.class));
    assertNotNull(foundFolder);
  }

  @Test
  public void getFolderByNameOrDefaultFolderDefault() {
    String folderName = null;
    Folder folder = new Folder();
    when(folderRepository.findByName("/")).thenReturn(folder);
    Folder foundFolder = persistenceService.getFolderByNameOrDefaultFolder(folderName);
    verify(folderRepository, times(0)).save(any(Folder.class));
    assertEquals(folder, foundFolder);
  }

  // new main, new representee, representor only,
  @Test
  public void getOrganisationFromInternalXroadMemberNoMember() throws DhxException {
    InternalXroadMember member = getMember("code", null);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(null);
    Organisation org = persistenceService.getOrganisationFromInternalXroadMember(member, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    assertEquals(true, org.getIsActive());
    assertEquals(member.getMemberClass(), org.getMemberClass());
    assertEquals(member.getName(), org.getName());
    assertEquals(member.getMemberCode(), org.getRegistrationCode());
    assertEquals(member.getSubsystemCode(), org.getSubSystem());
    assertEquals(member.getXroadInstance(), org.getXroadInstance());
    assertEquals(true, org.getDhxOrganisation());
  }

  @Test
  public void getOrganisationFromInternalXroadMemberFound() throws DhxException {
    InternalXroadMember member = getMember("code", null);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(foundOrg);
    Organisation org = persistenceService.getOrganisationFromInternalXroadMember(member, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    assertEquals(foundOrg, org);
    assertEquals(true, org.getIsActive());
    assertEquals(member.getMemberClass(), org.getMemberClass());
    assertEquals(member.getName(), org.getName());
    assertEquals(member.getMemberCode(), org.getRegistrationCode());
    assertEquals(member.getSubsystemCode(), org.getSubSystem());
    assertEquals(member.getXroadInstance(), org.getXroadInstance());
    assertEquals(true, org.getDhxOrganisation());
  }

  @Test
  public void getOrganisationFromInternalXroadMemberFoundRepresentorOnly() throws DhxException {
    DhxRepresentee representee = new DhxRepresentee("code2", new Date(), null, null, "system");
    InternalXroadMember member = getMember("code", representee);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(foundOrg);
    Organisation org = persistenceService.getOrganisationFromInternalXroadMember(member, true);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    assertEquals(foundOrg, org);
  }

  @Test
  public void getOrganisationFromInternalXroadMemberFoundRepresenteeFound() throws DhxException {
    DhxRepresentee representee = new DhxRepresentee("code2", new Date(), null, null, "system");
    InternalXroadMember member = getMember("code", representee);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(foundOrg);
    Organisation foundOrgRepresentee = new Organisation();
    foundOrgRepresentee.setOrganisationId(11);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code2", "system"))
        .thenReturn(foundOrgRepresentee);
    Organisation org = persistenceService.getOrganisationFromInternalXroadMember(member, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code2",
        "system");
    verify(organisationRepository, times(2)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    assertEquals(foundOrgRepresentee, org);
    assertEquals(true, org.getIsActive());
    assertNull(org.getMemberClass());
    assertEquals(member.getRepresentee().getRepresenteeName(), org.getName());
    assertEquals(member.getRepresentee().getRepresenteeCode(), org.getRegistrationCode());
    assertEquals(member.getRepresentee().getRepresenteeSystem(), org.getSubSystem());
    assertNull(org.getXroadInstance());
    assertEquals(true, org.getDhxOrganisation());
    assertEquals(foundOrg, org.getRepresentor());
    assertNotNull(org.getRepresenteeStart());
    assertNull(org.getRepresenteeEnd());
  }

  @Test
  public void getOrganisationFromInternalXroadMemberFoundRepresenteeNotFound()
      throws DhxException {
    DhxRepresentee representee = new DhxRepresentee("code2", new Date(), null, null, "system");
    InternalXroadMember member = getMember("code", representee);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(foundOrg);
    Organisation foundOrgRepresentee = new Organisation();
    foundOrgRepresentee.setOrganisationId(11);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code2", "system"))
        .thenReturn(null);
    Organisation org = persistenceService.getOrganisationFromInternalXroadMember(member, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code2",
        "system");
    verify(organisationRepository, times(2)).findByRegistrationCodeAndSubSystem(
        Mockito.anyString(),
        Mockito.anyString());
    assertNotEquals(foundOrgRepresentee, org);
    assertEquals(true, org.getIsActive());
    assertNull(org.getMemberClass());
    assertEquals(member.getRepresentee().getRepresenteeName(), org.getName());
    assertEquals(member.getRepresentee().getRepresenteeCode(), org.getRegistrationCode());
    assertEquals(member.getRepresentee().getRepresenteeSystem(), org.getSubSystem());
    assertNull(org.getXroadInstance());
    assertEquals(true, org.getDhxOrganisation());
    assertEquals(foundOrg, org.getRepresentor());
    assertNotNull(org.getRepresenteeStart());
    assertNull(org.getRepresenteeEnd());
  }

  @Test
  public void getOrganisationFromInternalXroadMemberNotFoundRepresentee() throws DhxException {
    DhxRepresentee representee = new DhxRepresentee("code2", new Date(), null, null, "system");
    InternalXroadMember member = getMember("code", representee);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(null);
    Organisation foundOrgRepresentee = new Organisation();
    foundOrgRepresentee.setOrganisationId(11);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code2", "system"))
        .thenReturn(null);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Trying to insert representee, but representor is not in database!");
    persistenceService.getOrganisationFromInternalXroadMember(member, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");

  }

  @Test
  public void getOrganisationFromInternalXroadMemberAndSave() throws DhxException {
    InternalXroadMember member = getMember("code", null);
    Organisation foundOrg = new Organisation();
    foundOrg.setOrganisationId(10);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(foundOrg);
    persistenceService.getOrganisationFromInternalXroadMemberAndSave(member, true, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");
    verify(organisationRepository, times(0)).save(any(Organisation.class));
  }

  @Test
  public void getOrganisationFromInternalXroadMemberAndSaveFound() throws DhxException {
    InternalXroadMember member = getMember("code", null);
    when(organisationRepository.findByRegistrationCodeAndSubSystem("code", "DHX"))
        .thenReturn(null);
    persistenceService.getOrganisationFromInternalXroadMemberAndSave(member, true, false);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("code", "DHX");
    verify(organisationRepository, times(1)).save(any(Organisation.class));
  }
}

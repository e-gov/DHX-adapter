package ee.bpw.dhx.server.persistence.service;


import static org.junit.Assert.assertEquals;
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
import ee.ria.dhx.server.persistence.service.CapsuleService;
import ee.ria.dhx.server.persistence.service.PersistenceService;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.service.AddressService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import javax.activation.DataHandler;

public class PersistenceServiceTest {
  

  @Mock
  AddressService addressService;

  @Mock
  OrganisationRepository organisationRepository;


  @Mock
  FolderRepository folderRepository;
  
  PersistenceService persistenceService;
  
  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    persistenceService = new PersistenceService();
    persistenceService.setAddressService(addressService);
    persistenceService.setFolderRepository(folderRepository);
    persistenceService.setOrganisationRepository(organisationRepository);
      Folder folder = new Folder();
    folder.setName("folder");
    persistenceService.setSpecialOrganisations("adit,kovtp,rt,eelnoud");
  }
  
  private InternalXroadMember getMember(String memberCode, DhxRepresentee representee) {
    return new InternalXroadMember("ee-dev", "GOV", memberCode, "DHX", "Name1", representee);
  }

  
  @Test
  public void findOrg () throws DhxException{
    String containerOrganisationId = "member1";
    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null)).thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(), Mockito.anyString());
    verify(organisationRepository, times(0)).findByRegistrationCodeAndSubSystem(Mockito.anyString(), Mockito.notNull(String.class));
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());
    
  }
  
  @Test
  public void findOrgSubsystem () throws DhxException{
    String containerOrganisationId = "system.member1";
    when(organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null)).thenReturn(null);
    
    InternalXroadMember member = getMember("member1", null);
    member.setSubsystemCode("system");
    when(addressService.getClientForMemberCode("member1", "system")).thenReturn(member);
    
    Organisation org = new Organisation();
    org.setRegistrationCode("member1");
    when(organisationRepository.findByRegistrationCodeAndSubSystem("member1", "system")).thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
    verify(addressService, times(1)).getClientForMemberCode("member1", "system");
    verify(organisationRepository, times(1)).findByRegistrationCodeAndSubSystem("member1", "system");
    verify(organisationRepository, times(0)).findBySubSystem(Mockito.anyString());
  }
  
  @Test
  public void findOrgSpecial () throws DhxException{
    String containerOrganisationId = "adit";
    Organisation org = new Organisation();
    org.setRegistrationCode("adit");
    when(organisationRepository.findBySubSystem("adit")).thenReturn(org);
    persistenceService.findOrg(containerOrganisationId);
    verify(organisationRepository, times(1)).findBySubSystem("adit");
    verify(organisationRepository, times(0)).findByRegistrationCodeAndSubSystem(Mockito.anyString(), Mockito.anyString());
    verify(addressService, times(0)).getClientForMemberCode(Mockito.anyString(), Mockito.anyString());
  }

}

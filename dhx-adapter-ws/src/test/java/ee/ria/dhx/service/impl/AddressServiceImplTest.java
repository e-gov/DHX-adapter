package ee.ria.dhx.service.impl;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.GlobalGroupType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.MemberClassType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.MemberType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.ObjectFactory;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.SharedParametersType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.SubsystemType;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.impl.AddressServiceImpl;
import ee.ria.dhx.ws.service.impl.DhxGateway;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.BDDMockito.Then;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.mail.internet.AddressException;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
public class AddressServiceImplTest {

  @Mock
  DhxImplementationSpecificService specificService;

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Mock
  DhxGateway dhxGateway;

  AddressServiceImpl addressService;

  @Mock
  SoapConfig config;


  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    addressService = new AddressServiceImpl();
    addressService.setConfig(config);
    addressService.setDhxMarshallerService(dhxMarshallerService);
    addressService.setDhxImplementationSpecificService(specificService);
  }

  @Test
  public void getAdresseeListNonEmptySaved() throws DhxException {
    List<InternalXroadMember> retList = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "class", "code", "subsystem", "name", null);
    retList.add(member);
    when(specificService.getAdresseeList())
        .thenReturn(retList);
    List<InternalXroadMember> adressees = addressService.getAdresseeList();
    assertEquals(1, adressees.size());
    assertEquals(member, adressees.get(0));
    Mockito.verify(specificService, Mockito.times(1)).getAdresseeList();

  }

  private SharedParametersType setReturningGlobalConfAndMock(SharedParametersType params)
      throws Exception {
    URL url = PowerMockito.mock(URL.class);
    URLConnection con = mock(URLConnection.class);
    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(url);
    when(url.openConnection()).thenReturn(con);
    InputStream stream = new InputStream() {

      @Override
      public int read() throws IOException {
        // TODO Auto-generated method stub
        return 0;
      }
    };
    when(con.getInputStream()).thenReturn(stream);
    PowerMockito.mockStatic(FileUtil.class);
    PowerMockito.when(
        FileUtil.zipUnpack(Mockito.any(InputStream.class), Mockito.any(String.class)))
        .thenReturn(stream);
    JAXBElement<SharedParametersType> jaxbElement = new JAXBElement(
        new QName(SharedParametersType.class.getSimpleName()), SharedParametersType.class, null);
    jaxbElement.setValue(params);
    when(dhxMarshallerService.unmarshall(Mockito.any(InputStream.class))).thenReturn(jaxbElement);
    when(config.getDhxSubsystemPrefix()).thenReturn("DHX");
    return params;
  }


  @PrepareForTest({FileUtil.class, AddressServiceImpl.class})
  @Test
  public void getAdresseeListEmpty() throws DhxException, Exception {
    SharedParametersType params = new SharedParametersType();
    GlobalGroupType group = new GlobalGroupType();
    group.setGroupCode("group");
    group.setDescription("Descr");
    params.getGlobalGroup().add(group);
    MemberType member = new MemberType();
    MemberClassType classType = new MemberClassType();
    classType.setCode("GOC");
    member.setMemberClass(classType);
    member.setMemberCode("100");
    member.setName("name");
    SubsystemType subsystem = new SubsystemType();
    subsystem.setSubsystemCode("subsystem");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);
    setReturningGlobalConfAndMock(params);
    List<InternalXroadMember> list = addressService.getAdresseeList();
    assertEquals(0, list.size());
  }

  @PrepareForTest({FileUtil.class, AddressServiceImpl.class})
  @Test
  public void getAdresseeListOnlyDirectDhx() throws DhxException, Exception {
    SharedParametersType params = new SharedParametersType();
    when(config.getXroadInstance()).thenReturn("ee");
    GlobalGroupType group = new GlobalGroupType();
    group.setGroupCode("group");
    group.setDescription("Descr");
    params.getGlobalGroup().add(group);
    MemberType member = new MemberType();
    MemberClassType classType = new MemberClassType();
    classType.setCode("GOC");
    member.setMemberClass(classType);
    member.setMemberCode("100");
    member.setName("name");
    SubsystemType subsystem = new SubsystemType();
    subsystem.setSubsystemCode("subsystem");
    member.getSubsystem().add(subsystem);


    member = new MemberType();
    classType = new MemberClassType();
    classType.setCode("GOV");
    member.setMemberClass(classType);
    member.setMemberCode("200");
    member.setName("name");
    subsystem = new SubsystemType();
    subsystem.setSubsystemCode("DHX");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);

    member = new MemberType();
    classType = new MemberClassType();
    classType.setCode("GOV");
    member.setMemberClass(classType);
    member.setMemberCode("300");
    member.setName("name2");
    subsystem = new SubsystemType();
    subsystem.setSubsystemCode("DHX.subsystem");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);

    setReturningGlobalConfAndMock(params);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    addressService.getAdresseeList();
    Mockito.verify(specificService).saveAddresseeList(argument.capture());
    List<InternalXroadMember> list = argument.getValue();
    assertEquals(2, list.size());
    assertEquals("200", list.get(0).getMemberCode());
    assertEquals("DHX", list.get(0).getSubsystemCode());
    assertNull(list.get(0).getRepresentee());
    assertEquals(null, list.get(0).getRepresentor());
    assertEquals("ee", list.get(0).getXroadInstance());
    assertEquals("name", list.get(0).getName());
    assertEquals("GOV", list.get(0).getMemberClass());

    assertEquals("300", list.get(1).getMemberCode());
    assertEquals("DHX.subsystem", list.get(1).getSubsystemCode());
  }


  // TODO: own representatives, not own representatives, representatives with miltiple subsystems
  // around different representors
  //if client is in dhx vahendajad grupp, but no DHX system is found
 //check representees dates 
  @PrepareForTest({FileUtil.class, AddressServiceImpl.class})
  //@Test
  public void getAdresseeListRepresentees() throws DhxException, Exception {
    SharedParametersType params = new SharedParametersType();
    when(config.getXroadInstance()).thenReturn("ee");
    when(config.getDhxRepresentationGroupName()).thenReturn("DHX vahendajad");
    GlobalGroupType group = new GlobalGroupType();
    group.setGroupCode("group");
    group.setDescription("Descr");
    params.getGlobalGroup().add(group);
    
    group = new GlobalGroupType();
    group.setGroupCode("DHX vahendajad");
    group.setDescription("DHX vahendajad");
    params.getGlobalGroup().add(group);
    XRoadClientIdentifierType client = new XRoadClientIdentifierType();
    client.setMemberClass("GOV");
    client.setMemberCode("400");
    client.setSubsystemCode("DHX");
    client.setXRoadInstance("ee");
    group.getGroupMember().add(client);
    
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
    DhxRepresentee representee = new DhxRepresentee("representee1", new Date(), null, "Representee 1", null);
    representees.add(representee);
    when(specificService.getRepresentationList()).thenReturn(representees);
    
    MemberType member = new MemberType();
    MemberClassType classType = new MemberClassType();
    classType.setCode("GOC");
    member.setMemberClass(classType);
    member.setMemberCode("100");
    member.setName("name");
    SubsystemType subsystem = new SubsystemType();
    subsystem.setSubsystemCode("subsystem");
    member.getSubsystem().add(subsystem);

    setReturningGlobalConfAndMock(params);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    addressService.getAdresseeList();
    Mockito.verify(specificService).saveAddresseeList(argument.capture());
    List<InternalXroadMember> list = argument.getValue();
    assertEquals(1, list.size());
    assertEquals("400", list.get(0).getMemberCode());
    assertEquals("DHX", list.get(0).getSubsystemCode());
    assertNotNull(list.get(0).getRepresentee());
    assertEquals(true, list.get(0).getRepresentor());
    assertEquals("ee", list.get(0).getXroadInstance());
    assertEquals("GOV", list.get(0).getMemberClass());
    assertEquals("representee1", list.get(0).getRepresentee().getRepresenteeCode());
    assertNull(list.get(0).getRepresentee().getRepresenteeSystem());
  }


}

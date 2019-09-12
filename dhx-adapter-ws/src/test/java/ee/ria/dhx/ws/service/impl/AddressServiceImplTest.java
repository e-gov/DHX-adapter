package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.RepresentationListResponse;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Representee;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Representees;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.GlobalGroupType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.MemberClassType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.MemberType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.SharedParametersType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.SubsystemType;
import ee.ria.dhx.util.ConversionUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.springframework.core.io.ClassPathResource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;


import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AddressServiceImplTest {

  @Mock
  DhxImplementationSpecificService specificService;

  @Mock
  DhxMarshallerService dhxMarshallerService;

  @Mock
  DhxGateway dhxGateway;

  @InjectMocks
  DhxOrganisationFactory dhxOrganisationFactory;

  AddressServiceImpl addressService;

  @Mock
  SoapConfig config;

  @Rule
  public ExpectedException expectedEx = ExpectedException.none();

  @Before
  public void init() {
    MockitoAnnotations.initMocks(this);
    addressService = new AddressServiceImpl();
    addressService.setConfig(config);
    addressService.setDhxGateway(dhxGateway);
    addressService.setDhxMarshallerService(dhxMarshallerService);
    addressService.setDhxImplementationSpecificService(specificService);
    addressService.setDhxOrganisationFactory(dhxOrganisationFactory);
    when(config.getXroadInstance()).thenReturn("ee");
    when(config.getDhxRepresentationGroupName()).thenReturn("DHX vahendajad");
    when(config.getDhxSubsystemPrefix()).thenReturn("DHX");
    //when(config.getGlobalConfLocation()).thenReturn("http://x-road.eu/packages/EE_public-anchor.xml");
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
    
    addressService = Mockito.spy(addressService);
    
    Mockito.doReturn(new InputStream() {
      
      @Override
      public int read() throws IOException {
        log.debug("AddressServiceImplTest.setReturningGlobalConfAndMock return 0");
        return 0;
      }
    }).when(addressService).getGlobalConfStream(Mockito.any(String.class));
    
    JAXBElement<SharedParametersType> jaxbElement = new JAXBElement<SharedParametersType>(
        new QName(SharedParametersType.class.getSimpleName()), SharedParametersType.class, null);
    jaxbElement.setValue(params);
    
    when(dhxMarshallerService.unmarshall(Mockito.any(InputStream.class))).thenReturn(jaxbElement);
    
    return params;
  }


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

  @SuppressWarnings("unchecked")
  @Test
  public void getAdresseeListOnlyDirectDhx() throws DhxException, Exception {
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
    @SuppressWarnings("rawtypes")
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    when(specificService.getAdresseeList()).thenReturn(new ArrayList<InternalXroadMember>());
    // here will be returned list that was given from mock. the real list will be captured using
    // ArgumentCaptor
    List<InternalXroadMember> listRet = addressService.getAdresseeList();
    assertEquals(0, listRet.size());
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


  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void getAdresseeListRepresentees() throws DhxException, Exception {
    SharedParametersType params = new SharedParametersType();
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

    RepresentationListResponse resp = new RepresentationListResponse();
    resp.setRepresentees(new Representees());
    Representee representee = new Representee();
    representee.setMemberCode("representee1");
    representee.setRepresenteeName("Repr name");
    representee.setRepresenteeSystem(null);
    representee.setStartDate(ConversionUtil.toGregorianCalendar(new Date()));
    resp.getRepresentees().getRepresentee().add(representee);
    when(dhxGateway.getRepresentationList(Mockito.any(InternalXroadMember.class))).thenReturn(
        resp);

    MemberType member = new MemberType();
    MemberClassType classType = new MemberClassType();
    classType.setCode("GOV");
    member.setMemberClass(classType);
    member.setMemberCode("400");
    member.setName("name");
    SubsystemType subsystem = new SubsystemType();
    subsystem.setSubsystemCode("DHX");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);

    setReturningGlobalConfAndMock(params);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    addressService.getAdresseeList();
    Mockito.verify(specificService).saveAddresseeList(argument.capture());
    List<InternalXroadMember> list = argument.getValue();
    assertEquals(2, list.size());
    assertEquals("400", list.get(0).getMemberCode());
    assertEquals("DHX", list.get(0).getSubsystemCode());
    assertEquals(true, list.get(0).getRepresentor());
    assertEquals("ee", list.get(0).getXroadInstance());
    assertEquals("GOV", list.get(0).getMemberClass());
    assertNull(list.get(0).getRepresentee());

    assertEquals("400", list.get(1).getMemberCode());
    assertEquals("DHX", list.get(1).getSubsystemCode());
    assertNotNull(list.get(1).getRepresentee());
    assertEquals(null, list.get(1).getRepresentor());
    assertEquals("ee", list.get(1).getXroadInstance());
    assertEquals("GOV", list.get(1).getMemberClass());
    assertEquals("representee1", list.get(1).getRepresentee().getRepresenteeCode());
    assertNull(list.get(1).getRepresentee().getRepresenteeSystem());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Test
  public void getAdresseeListRepresenteesMultiple() throws DhxException, Exception {
    SharedParametersType params = new SharedParametersType();
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

    client = new XRoadClientIdentifierType();
    client.setMemberClass("GOV");
    client.setMemberCode("401");
    client.setSubsystemCode("DHX.representees");
    client.setXRoadInstance("ee");
    group.getGroupMember().add(client);

    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("06.12.2016 12:32");

    RepresentationListResponse resp = new RepresentationListResponse();
    resp.setRepresentees(new Representees());
    // regular representee
    Representee representee = new Representee();
    representee.setMemberCode("representee1");
    representee.setRepresenteeName("Repr name");
    representee.setRepresenteeSystem(null);
    representee.setStartDate(ConversionUtil.toGregorianCalendar(date));
    resp.getRepresentees().getRepresentee().add(representee);
    when(dhxGateway.getRepresentationList(Mockito.argThat(new InternalXroadMemberMetcher("400"))))
        .thenReturn(resp);

    // representee with end date
    resp = new RepresentationListResponse();
    resp.setRepresentees(new Representees());
    // regular representee
    representee = new Representee();
    representee.setMemberCode("representee2");
    representee.setRepresenteeName("Repr name");
    representee.setRepresenteeSystem(null);
    representee.setStartDate(ConversionUtil.toGregorianCalendar(date));
    representee.setEndDate(ConversionUtil.toGregorianCalendar(date));
    resp.getRepresentees().getRepresentee().add(representee);

    // representee with same memberCode as the first one, but with different representor
    representee = new Representee();
    representee.setMemberCode("representee1");
    representee.setRepresenteeName("Repr name");
    representee.setRepresenteeSystem(null);
    representee.setRepresenteeSystem("subsystem");
    representee.setStartDate(ConversionUtil.toGregorianCalendar(date));
    representee.setEndDate(ConversionUtil.toGregorianCalendar(date));
    resp.getRepresentees().getRepresentee().add(representee);
    when(dhxGateway.getRepresentationList(Mockito.argThat(new InternalXroadMemberMetcher("401"))))
        .thenReturn(resp);

    MemberType member = new MemberType();
    MemberClassType classType = new MemberClassType();
    classType.setCode("GOV");
    member.setMemberClass(classType);
    member.setMemberCode("400");
    member.setName("name");
    SubsystemType subsystem = new SubsystemType();
    subsystem.setSubsystemCode("DHX");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);

    member = new MemberType();
    classType = new MemberClassType();
    classType.setCode("GOV");
    member.setMemberClass(classType);
    member.setMemberCode("401");
    member.setName("name 2");
    subsystem = new SubsystemType();
    subsystem.setSubsystemCode("DHX.representees");
    member.getSubsystem().add(subsystem);
    params.getMember().add(member);

    setReturningGlobalConfAndMock(params);
    ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
    addressService.getAdresseeList();
    
    verify(dhxGateway, times(2)).getRepresentationList(Mockito.any(InternalXroadMember.class));
    verify(specificService).saveAddresseeList(argument.capture());
    List<InternalXroadMember> list = argument.getValue();
    assertEquals(5, list.size());
    assertEquals("400", list.get(0).getMemberCode());
    assertEquals("DHX", list.get(0).getSubsystemCode());
    assertEquals(true, list.get(0).getRepresentor());
    assertEquals("ee", list.get(0).getXroadInstance());
    assertEquals("GOV", list.get(0).getMemberClass());
    assertNull(list.get(0).getRepresentee());

    assertEquals("401", list.get(1).getMemberCode());
    assertEquals("DHX.representees", list.get(1).getSubsystemCode());
    assertEquals(true, list.get(1).getRepresentor());
    assertEquals("ee", list.get(1).getXroadInstance());
    assertEquals("GOV", list.get(1).getMemberClass());
    assertNull(list.get(1).getRepresentee());

    assertEquals("400", list.get(2).getMemberCode());
    assertEquals("DHX", list.get(2).getSubsystemCode());
    assertNotNull(list.get(2).getRepresentee());
    assertEquals(null, list.get(2).getRepresentor());
    assertEquals("ee", list.get(2).getXroadInstance());
    assertEquals("GOV", list.get(2).getMemberClass());
    assertEquals("representee1", list.get(2).getRepresentee().getRepresenteeCode());
    assertNull(list.get(2).getRepresentee().getRepresenteeSystem());

    assertEquals("401", list.get(3).getMemberCode());
    assertEquals("DHX.representees", list.get(3).getSubsystemCode());
    assertNotNull(list.get(3).getRepresentee());
    assertEquals(null, list.get(3).getRepresentor());
    assertEquals("ee", list.get(3).getXroadInstance());
    assertEquals("GOV", list.get(3).getMemberClass());
    assertEquals("representee2", list.get(3).getRepresentee().getRepresenteeCode());
    assertNull(list.get(3).getRepresentee().getRepresenteeSystem());


    assertEquals("401", list.get(4).getMemberCode());
    assertEquals("DHX.representees", list.get(4).getSubsystemCode());
    assertNotNull(list.get(4).getRepresentee());
    assertEquals(null, list.get(4).getRepresentor());
    assertEquals("ee", list.get(4).getXroadInstance());
    assertEquals("GOV", list.get(4).getMemberClass());
    assertEquals("representee1", list.get(4).getRepresentee().getRepresenteeCode());
    assertEquals("subsystem", list.get(4).getRepresentee().getRepresenteeSystem());
    assertEquals("06.12.2016 12:32", sdf.format(list.get(4).getRepresentee().getStartDate()));
    assertEquals("06.12.2016 12:32", sdf.format(list.get(4).getRepresentee().getEndDate()));
  }

  private static final class InternalXroadMemberMetcher
      extends ArgumentMatcher<InternalXroadMember> {

    private final String memberCode;

    public InternalXroadMemberMetcher(String memberCode) {
      this.memberCode = memberCode;
    }

    @Override
    public boolean matches(Object argument) {
      if (argument == null || ((InternalXroadMember) argument).getMemberCode() == null) {
        return false;
      }
      return ((InternalXroadMember) argument).getMemberCode().equals(memberCode);
    }
  }

  @Test
  public void getClientForMemberCode() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code1", null);
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeNotRepresentee() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code1", null);
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeNotFound() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, null, "Name", null);
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    member = new InternalXroadMember("ee", "GOV", "code3", "DHX", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Recipient is not found in address list. memberCode: code1");
    addressService.getClientForMemberCode("code1", null);
  }

  @Test
  public void getClientForMemberCodeSubsystems() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX.subsystem", "Name", null);
    members.add(member);
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code1", null);
    assertEquals(member, memberFound);

  }

  @Test
  public void getClientForMemberCodeSubsystems2() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX.subsystem", "Name", null);
    members.add(member);
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code1", "DHX");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeSubsystems3() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX.subsystem", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code1", "subsystem");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeSubsystems4() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX.subsystem", "Name", null);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound =
        addressService.getClientForMemberCode("code1", "DHX.subsystem");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresentee() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    DhxRepresentee representee = new DhxRepresentee("code2", date, null, "Name", null);
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code2", null);
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresenteeSubsystem() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, null, "Name", "DHX.system2");
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    representee = new DhxRepresentee("code2", date, null, "Name", "system");
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code2", "system");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresenteeSubsystem2() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, null, "Name", "DHX.system2");
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    representee = new DhxRepresentee("code2", date, null, "Name", "system");
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound =
        addressService.getClientForMemberCode("code2", "DHX.system");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresenteeSubsystem3() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, null, "Name", "system");
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    representee = new DhxRepresentee("code2", date, null, "Name", "DHX.system2");
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound =
        addressService.getClientForMemberCode("code2", "DHX.system2");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresenteeSubsystem4() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code 2", date, null, "Name", "system");
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", null);
    members.add(member);
    representee = new DhxRepresentee("code2", date, null, "Name", "DHX.system2");
    member = new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code2", "system2");
    assertEquals(member, memberFound);
  }

  @Test
  public void getClientForMemberCodeRepresenteeOutdated() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, date, "Name", null);
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    expectedEx.expect(DhxException.class);
    expectedEx.expectMessage("Recipient is not found in address list. memberCode: code2");
    addressService.getClientForMemberCode("code2", null);
  }

  @Test
  public void getClientForMemberCodeRepresenteeValidAndOutdated() throws Exception {
    List<InternalXroadMember> members = new ArrayList<InternalXroadMember>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
    Date date = sdf.parse("05.12.2016 12:32");
    DhxRepresentee representee = new DhxRepresentee("code2", date, date, "Name", null);
    InternalXroadMember member =
        new InternalXroadMember("ee", "GOV", "code1", "DHX", "Name", representee);
    members.add(member);
    representee = new DhxRepresentee("code2", date, null, "Name", null);
    member = new InternalXroadMember("ee", "GOV", "code3", "DHX", "Name", representee);
    members.add(member);
    when(specificService.getAdresseeList()).thenReturn(members);
    InternalXroadMember memberFound = addressService.getClientForMemberCode("code2", null);
    assertEquals(member, memberFound);
  }

}

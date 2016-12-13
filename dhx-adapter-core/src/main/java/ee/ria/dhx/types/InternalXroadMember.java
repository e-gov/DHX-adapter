package ee.ria.dhx.types;

import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.xroad.MemberType;

/**
 * Internal representation of X-road member. Among X-road data contains also representees data.
 * 
 * @author Aleksei Kokarev
 *
 */
public class InternalXroadMember {

  /**
   * Create internal XroadMember from XRoadClientIdentifierType(X-road object).
   * 
   * @param xrdClient - x-road client
   */
  public InternalXroadMember(XRoadClientIdentifierType xrdClient) {
    this.xroadInstance = xrdClient.getXRoadInstance();
    this.memberClass = xrdClient.getMemberClass();
    this.memberCode = xrdClient.getMemberCode();
    this.subsystemCode = xrdClient.getSubsystemCode();
  }

  /**
   * Create internal XroadMember from XRoadServiceIdentifierType(X-road object).
   * 
   * @param xrdService - x-road service
   */
  public InternalXroadMember(XRoadServiceIdentifierType xrdService) {
    this.xroadInstance = xrdService.getXRoadInstance();
    this.memberClass = xrdService.getMemberClass();
    this.memberCode = xrdService.getMemberCode();
    this.subsystemCode = xrdService.getSubsystemCode();

    this.serviceCode = xrdService.getServiceCode();
    this.serviceVersion = xrdService.getServiceVersion();
  }

  /**
   * Create internal XroadMember.
   * 
   * @param xroadInstance - name of X-road instance
   * @param member - X-road member(X-road obvject)
   * @param subsytemCode - X-road subsystem
   * @param name - X-road members name
   */
  public InternalXroadMember(String xroadInstance, MemberType member, String subsytemCode,
      String name) {
    this.xroadInstance = xroadInstance;
    this.memberClass = member.getMemberClass().getCode();
    this.memberCode = member.getMemberCode();
    this.subsystemCode = subsytemCode;
    this.name = name;
  }

  /**
   * Create internal XroadMember from another XroadMember and representee.
   * 
   * @param member - XroadMember from which nto create new XroadMember
   * @param representee - representee to put to new XroadMember
   */
  public InternalXroadMember(InternalXroadMember member, DhxRepresentee representee) {
    this.xroadInstance = member.getXroadInstance();
    this.memberClass = member.getMemberClass();
    this.memberCode = member.getMemberCode();
    this.subsystemCode = member.getSubsystemCode();
    this.representee = representee;
    this.name = member.getName();
  }

  /**
   * Create internal XroadMember.
   * 
   * @param xroadInstance - name of members X-road instance
   * @param memberClass - name of members X-road class
   * @param memberCode - members X-road member code
   * @param subsystemCode - name on X-road subsystem
   * @param name - X-road members name
   * @param representee - representee to put to new XroadMember
   */
  public InternalXroadMember(String xroadInstance, String memberClass, String memberCode,
      String subsystemCode, String name, DhxRepresentee representee) {
    this.xroadInstance = xroadInstance;
    this.memberClass = memberClass;
    this.memberCode = memberCode;
    this.subsystemCode = subsystemCode;
    this.name = name;
    this.representee = representee;

  }

  private String xroadInstance;
  private String memberClass;
  private String memberCode;
  private String subsystemCode;
  private String name;

  // service variables
  private String serviceCode;
  private String serviceVersion;

  private DhxRepresentee representee;

  private Boolean representor;

  private String userId;


  @Override
  public String toString() {
    return "X-road member "
        + xroadInstance
        + "/"
        + memberClass
        + "/"
        + memberCode
        + "/"
        + subsystemCode
        + (representee == null ? "" : ", representee: "
            + representee.getRepresenteeCode() + ", system:" + representee.getRepresenteeSystem());
  }


  /**
   * Returns the xroadInstance.
   * @return the xroadInstance
   */
  public String getXroadInstance() {
    return xroadInstance;
  }

  /**
   * Sets the xroadInstance.
   * @param xroadInstance the xroadInstance to set
   */
  public void setXroadInstance(String xroadInstance) {
    this.xroadInstance = xroadInstance;
  }

  /**
   * Returns the memberClass.
   * @return the memberClass
   */
  public String getMemberClass() {
    return memberClass;
  }

  /**
   * Sets the memberClass.
   * @param memberClass the memberClass to set
   */
  public void setMemberClass(String memberClass) {
    this.memberClass = memberClass;
  }

  /**
   * Returns the memberCode.
   * @return the memberCode
   */
  public String getMemberCode() {
    return memberCode;
  }

  /**
   * Sets the memberCode.
   * @param memberCode the memberCode to set
   */
  public void setMemberCode(String memberCode) {
    this.memberCode = memberCode;
  }

  /**
   * Returns the subsystemCode.
   * @return the subsystemCode
   */
  public String getSubsystemCode() {
    return subsystemCode;
  }

  /**
   * Sets the subsystemCode.
   * @param subsystemCode the subsystemCode to set
   */
  public void setSubsystemCode(String subsystemCode) {
    this.subsystemCode = subsystemCode;
  }

  /**
   * Returns the name.
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the serviceCode.
   * @return the serviceCode
   */
  public String getServiceCode() {
    return serviceCode;
  }

  /**
   * Sets the serviceCode.
   * @param serviceCode the serviceCode to set
   */
  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  /**
   * Returns the serviceVersion.
   * @return the serviceVersion
   */
  public String getServiceVersion() {
    return serviceVersion;
  }

  /**
   * Sets the serviceVersion.
   * @param serviceVersion the serviceVersion to set
   */
  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }

  /**
   * Returns the representee.
   * @return the representee
   */
  public DhxRepresentee getRepresentee() {
    return representee;
  }

  /**
   * Sets the representee.
   * @param representee the representee to set
   */
  public void setRepresentee(DhxRepresentee representee) {
    this.representee = representee;
  }

  /**
   * Returns the representor.
   * @return the representor
   */
  public Boolean getRepresentor() {
    return representor;
  }

  /**
   * Sets the representor.
   * @param representor the representor to set
   */
  public void setRepresentor(Boolean representor) {
    this.representor = representor;
  }

  /**
   * Returns the userId.
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the userId.
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

}

package ee.ria.dhx.types;


/**
 * Represents document sender or recipient organisation. Helps to easily define to whom or from who
 * the document is really sent. Recipient might be not obvious if document is sent to representee.
 * This class contains representees data if document is sent to/from representee and direct reciever
 * data if it is sent directly, not to/from representee. Also is usefull to check if two
 * organisations are the same, using equals method.
 * 
 * @author Aleksei Kokarev
 *
 */

public class DhxOrganisation {

  private String code;
  private String system;
  private String dhxSubsystemPrefix;

  /**
   * DhxOrganisation consctructor.
   * @param member the member to create DhxOrganisation from
   * @param dhxSubsystemPrefix DHX subsystem prefix to use when creating DhxOrganisation
   */
  public DhxOrganisation(InternalXroadMember member, String dhxSubsystemPrefix) {
    if (member.getRepresentee() != null) {
      this.code = member.getRepresentee().getRepresenteeCode();
      this.system = member.getRepresentee().getRepresenteeSystem();
    } else {
      this.code = member.getMemberCode();
      this.system = member.getSubsystemCode();
    }
    this.dhxSubsystemPrefix = dhxSubsystemPrefix;
  }

  /**
   * DhxOrganisation consctructor.
   * 
   * @param code code of the reciepint. might be either X-road memberCode or representees code
   * @param system system of the recipient. migth be either X-road subSystemCode or representees
   *        system
   * @param dhxSubsystemPrefix DHX system default prefix. used to define if two systems are euqal
   *        even if one of them presented without prefix.
   */
  public DhxOrganisation(String code, String system, String dhxSubsystemPrefix) {
    this.code = code;
    this.system = system;
    this.dhxSubsystemPrefix = dhxSubsystemPrefix;
  }

  /**
   * Returns code in upper case.
   * @return code in upper case
   */
  public String getCodeUpper() {
    return code.toUpperCase();
  }

  /**
   * Returns the code.
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the system.
   * @return the system
   */
  public String getSystem() {
    return system;
  }

  /**
   * Returns the system, if system is null then empty string is returned.
   * @return the system, if system is null then empty string is returned
   */
  private String getNotNullSystem() {
    if (system == null) {
      return "";
    }
    return system.toUpperCase();
  }


  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof DhxOrganisation)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    DhxOrganisation recipient = (DhxOrganisation) obj;
    if (recipient.getCodeUpper().equals(this.getCodeUpper())
        && recipient.getAdaptedSystem(dhxSubsystemPrefix).equals(
            this.getAdaptedSystem(dhxSubsystemPrefix))) {
      return true;
    }
    return false;
  }

  /**
   * add prefix to system if it is not present.
   * 
   * @param dhxSubsystemPrefix - DHX system default prefix. used to define if two systems are euqal
   *        even if one of them presented without prefix.
   * @return - adpted system
   */
  private String getAdaptedSystem(String dhxSubsystemPrefix) {
    String adaptedSystem = system;
    if (adaptedSystem == null) {
      adaptedSystem = dhxSubsystemPrefix;
    }
    if (!adaptedSystem.startsWith(dhxSubsystemPrefix)
        && !adaptedSystem.equals(dhxSubsystemPrefix)) {
      adaptedSystem = dhxSubsystemPrefix + "." + adaptedSystem;
    }
    return adaptedSystem.toUpperCase();
  }

  /**
   * Function to check capsule recipient. Accepts if capsule recipient equals to code or system or
   * their combination either with or without DHX subsystem prefix.
   * 
   * @param capsuleRecipient - recipient from capsule
   * @return - true if recipine from capsule equals to recipient defined in object
   */
  public Boolean equalsToCapsuleOrganisation(String capsuleRecipient) {
    String capsuleRecipientUp = capsuleRecipient.toUpperCase();
    if (capsuleRecipientUp.equals(getCodeUpper())
        || capsuleRecipientUp.equals(getNotNullSystem() + "." + getCodeUpper())
        || capsuleRecipientUp.equals(getAdaptedSystem(dhxSubsystemPrefix) + "." + getCodeUpper())
        || capsuleRecipientUp.equals(getNotNullSystem())
        || capsuleRecipientUp.equals(getAdaptedSystem(dhxSubsystemPrefix))) {
      return true;
    }
    return false;
  }

  /**
   * Returns the dhxSubsystemPrefix.
   * @return the dhxSubsystemPrefix
   */
  public String getDhxSubsystemPrefix() {
    return dhxSubsystemPrefix;
  }

  /**
   * Sets the dhxSubsystemPrefix.
   * @param dhxSubsystemPrefix the dhxSubsystemPrefix to set
   */
  public void setDhxSubsystemPrefix(String dhxSubsystemPrefix) {
    this.dhxSubsystemPrefix = dhxSubsystemPrefix;
  }

  /**
   * Returns the code.
   * @param code the code to set
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Sets the system.
   * @param system the system to set
   */
  public void setSystem(String system) {
    this.system = system;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "DhxOrganisation [code=" + code + ", system=" + system + ", dhxSubsystemPrefix="
        + dhxSubsystemPrefix + "]";
  }



}

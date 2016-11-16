package ee.bpw.dhx.model;

import lombok.Setter;

/**
 * Represents recipient of the document. Helps to easily define to whom the document is really sent.
 * Recipient might be not obvious if document is sent to representee. This class contains
 * representees data if document is sent to representee and direct reciever data if it is sent
 * directly, not to representee.
 * 
 * @author Aleksei Kokarev
 *
 */
@Setter
public class DhxRecipient {

  private String code;
  private String system;

  /**
   * Recipient consctructor.
   * 
   * @param code - code of the reciepint. might be either X-road memberCode or representees code
   * @param system - system of the recipient. migth be either X-road subSystemCode or representees
   *        system
   */
  public DhxRecipient(String code, String system) {
    this.code = code;
    this.system = system;
  }

  /**
   * Recipient consctructor.
   * 
   */
  public DhxRecipient() {}

  public String getCodeUpper() {
    return code.toUpperCase();
  }

  public String getCode() {
    return code;
  }

  public String getSystem() {
    return system;
  }

  private String getNotNullSystem() {
    if (system == null) {
      return "";
    }
    return system.toUpperCase();
  }


  /**
   * 
   * @param obj - object to compare to
   * @param dhxSubsystemPrefix - DHX system default prefix. used to define if two systems are euqal
   *        even if one of them presented without prefix.
   * @return - returns true if both recipients are equal
   */
  public boolean equals(Object obj, String dhxSubsystemPrefix) {
    if (!(obj instanceof DhxRecipient)) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    DhxRecipient recipient = (DhxRecipient) obj;
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
   * @param dhxSubsystemPrefix - DHX system default prefix. used to define if two systems are euqal
   *        even if one of them presented without prefix.
   * @return - true if recipine from capsule equals to recipient defined in object
   */
  public Boolean equalsToCapsuleRecipient(String capsuleRecipient, String dhxSubsystemPrefix) {
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

  @Override
  public String toString() {
    return "code: " + code
        + ", system: " + system;
  }


}

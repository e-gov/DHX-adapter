package ee.ria.dhx.types;


/**
 * Internal representation of adressee defined in capsule.
 * 
 * @author Aleksei Kokarev
 *
 */

public class CapsuleAdressee {

  public CapsuleAdressee(String adresseeCode) {
    this.adresseeCode = adresseeCode;
  }

  /**
   * CapsuleAdressee constructor.
   * @param adresseeCode code of the capsule addressee
   * @param personalCode personal code of the capsule addressee
   * @param structuralUnit structural unit of the capsule addressee
   */
  public CapsuleAdressee(String adresseeCode, String personalCode, String structuralUnit) {
    this.adresseeCode = adresseeCode;
    this.personalCode = personalCode;
    this.structuralUnit = structuralUnit;
  }

  String adresseeCode;
  String personalCode;
  String structuralUnit;


  /**
   * Returns the adresseeCode.
   * @return the adresseeCode
   */
  public String getAdresseeCode() {
    return adresseeCode;
  }

  /**
   * Sets the adresseeCode.
   * @param adresseeCode the adresseeCode to set
   */
  public void setAdresseeCode(String adresseeCode) {
    this.adresseeCode = adresseeCode;
  }

  /**
   * Returns the personalCode.
   * @return the personalCode
   */
  public String getPersonalCode() {
    return personalCode;
  }

  /**
   * Sets the personalCode.
   * @param personalCode the personalCode to set
   */
  public void setPersonalCode(String personalCode) {
    this.personalCode = personalCode;
  }

  /**
   * Returns the structuralUnit.
   * @return the structuralUnit
   */
  public String getStructuralUnit() {
    return structuralUnit;
  }

  /**
   * Sets the structuralUnit.
   * @param structuralUnit the structuralUnit to set
   */
  public void setStructuralUnit(String structuralUnit) {
    this.structuralUnit = structuralUnit;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CapsuleAdressee [adresseeCode=" + adresseeCode + ", personalCode=" + personalCode
        + ", structuralUnit=" + structuralUnit + "]";
  }



}

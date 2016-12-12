package ee.ria.dhx.types;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Internal representation of adressee defined in capsule.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
@ToString
public class CapsuleAdressee {

  public CapsuleAdressee(String adresseeCode) {
    this.adresseeCode = adresseeCode;
  }
  
  public CapsuleAdressee(String adresseeCode, String personalCode, String structuralUnit) {
    this.adresseeCode = adresseeCode;
    this.personalCode = personalCode;
    this.structuralUnit = structuralUnit;
  }

  String adresseeCode;
  String personalCode;
  String structuralUnit;

}

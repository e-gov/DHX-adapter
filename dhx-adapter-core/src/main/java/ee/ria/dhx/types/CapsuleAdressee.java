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

  String adresseeCode;

}

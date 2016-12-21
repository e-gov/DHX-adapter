package ee.ria.dhx.server.persistence.enumeration;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Enumeration of the document statuses with IDs and names(for DVk backward compatibility).
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public enum StatusEnum {

  IN_PROCESS("saatmisel", 101), RECEIVED("saadetud", 102), FAILED("katkestatud", 103);

  @Getter
  String classificatorName;

  @Getter
  Integer classificatorId;

  private StatusEnum(String classificatorName, Integer classificatorId) {
    this.classificatorName = classificatorName;
    this.classificatorId = classificatorId;
  }


  /**
   * Finds StatusEnum by classificatorId.
   * 
   * @param classificatorId classificatorId to find StatusEnum for
   * @return found StatusEnum
   * @throws DhxException thrown if error occurs
   */
  public static StatusEnum forClassificatorId(Integer classificatorId)
      throws DhxException {
    if (classificatorId != null) {
      for (StatusEnum status : StatusEnum.values()) {
        if (classificatorId.equals(status.getClassificatorId())) {
          log.debug("Found XSD version for class. classificatorId: {}",
              classificatorId.toString()
                  + " status:" + status.toString());
          return status;
        }
      }
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Unknown class. No XSD version is found for that class. Class: "
              + classificatorId.toString());
    }
    return null;
  }
}

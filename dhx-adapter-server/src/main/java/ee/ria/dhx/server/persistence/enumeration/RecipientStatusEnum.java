package ee.ria.dhx.server.persistence.enumeration;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * Enumeration of the recipient statuses with IDs and names(for DVk backward compatibility).
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public enum RecipientStatusEnum {

  NO_DOCUMENT("Dokumente on puudu (Pooleli)", 1), IN_QUEUE("Järjekorras", 2), WAITING("Ootel",
      3), ENDED(
          "Lõpetatud", 4), REJECTED("Tagasi lükatud", 5), TO_DO("Teha", 6), IN_WORK("Töötlemisel",
              7), ACCEPTED("Aktsepteeritud (Võetud töösse)", 8), SAVED(
                  "Salvestatud", 9), ARCHIVED("Arhiveeritud", 10), SENT("Saadetud", 11);

  @Getter
  String classificatorName;

  @Getter
  Integer classificatorId;

  private RecipientStatusEnum(String classificatorName, Integer classificatorId) {
    this.classificatorName = classificatorName;
    this.classificatorId = classificatorId;
  }


  /**
   * Finds RecipientStatusEnum by classificatorId.
   * 
   * @param classificatorId classificatorId to find RecipientStatusEnum for
   * @return found RecipientStatusEnum
   * @throws DhxException thrown if error occurs
   */
  public static RecipientStatusEnum forClassificatorId(Integer classificatorId)
      throws DhxException {
    if (classificatorId != null) {
      for (RecipientStatusEnum status : RecipientStatusEnum.values()) {
        if (classificatorId.equals(status.getClassificatorId())) {
          log.debug("Found XSD version for class. classificatorId: {} status: {}",
              classificatorId, status);
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

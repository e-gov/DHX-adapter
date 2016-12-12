package ee.ria.dhx.server.persistence.enumeration;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum RecipientStatusEnum {

  NO_DOCUMENT("Dokumente on puudu (Pooleli)", 1), IN_QUEUE("Järjekorras", 2), WAITING("Ootel", 3), ENDED(
      "Lõpetatud", 4), REJECTED("Tagasi lükatud", 5)
  , TO_DO("Teha", 6), IN_WORK("Töötlemisel", 7), ACCEPTED("Aktsepteeritud (Võetud töösse)", 8), SAVED(
      "Salvestatud", 9), ARCHIVED("Arhiveeritud", 10)
  , SENT("Saadetud", 11);

  @Getter
  String classificatorName;

  @Getter
  Integer classificatorId;

  private RecipientStatusEnum(String classificatorName, Integer classificatorId) {
    this.classificatorName = classificatorName;
    this.classificatorId = classificatorId;
  }


  /**
   * Finds StatusEnum by classificatorId
   * 
   * @param classificatorId
   * @return
   * @throws DhxException
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

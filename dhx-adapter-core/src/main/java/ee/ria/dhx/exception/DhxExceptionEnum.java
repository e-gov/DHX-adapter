package ee.ria.dhx.exception;

/**
 * Enumeration which contains codes of the errors that might occur in DHX application.
 * 
 * @author Aleksei Kokarev
 *
 */
public enum DhxExceptionEnum {

  CAPSULE_VALIDATION_ERROR("DHX.Validation", true), DUPLICATE_PACKAGE("DHX.Duplicate",
      true), WRONG_RECIPIENT("DHX.InvalidAddressee", true), OVER_MAX_SIZE("DHX.SizeLimitExceeded",
          true), WRONG_SENDER("DHX.SenderDoesNotMatch", true), FILE_ERROR("FILE_ERROR",
              false), WS_ERROR("WS_ERROR", false), EXTRACTION_ERROR("EXCTRACTION_ERROR",
                  false), TECHNICAL_ERROR("TECHNICAL_ERROR", false), DATA_ERROR("DATA_ERROR",
                      false), NOT_IMPLEMENTED("NOT_IMPLEMENTED", false), XSD_VERSION_ERROR(
                          "XSD_VERSION_ERROR",
                          false), PROTOCOL_VERSION_ERROR("DHX.UnsupportedVersion", true);

  // error code which is returned in SOAP services(custom fault element)
  private String codeForService;
  private Boolean businessException;


  private DhxExceptionEnum(String codeForService, Boolean businessException) {
    this.codeForService = codeForService;
    this.businessException = businessException;
  }

  /**
   * Returns error code which is returned in SOAP services(custom fault element).
   * 
   * @return - error code for SOAP services
   */
  public String getCodeForService() {
    return codeForService;
  }

  /**
   * is exception a business exception. Business exceptions are returned as custom fault element
   * with predefined exception code.
   * 
   * @return - is exception a business exception
   */
  public Boolean isBusinessException() {
    return businessException;
  }
}

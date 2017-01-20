package ee.ria.dhx.exception;


/**
 * DHX specific exception. contains error code.
 * 
 * @author Aleksei Kokarev
 *
 */

public class DhxException extends Exception {

  private DhxExceptionEnum exceptionCode;

  public DhxException(DhxExceptionEnum exceptionCode, String message) {
    super(message);
    this.setExceptionCode(exceptionCode);
  }

  public DhxException(String message) {
    super(message);
    this.setExceptionCode(DhxExceptionEnum.TECHNICAL_ERROR);
  }

  public DhxException(String message, Exception cause) {
    super(message, cause);
    this.setExceptionCode(DhxExceptionEnum.TECHNICAL_ERROR);
  }


  public DhxException(DhxExceptionEnum exceptionCode, String message, Exception cause) {
    super(message, cause);
    this.setExceptionCode(exceptionCode);
  }

  @Override
  public String getMessage() {
    String message = super.getMessage();
    return "DHXException code: " + exceptionCode.getCodeForService() + " " + message;
  }

  /**
   * Returns the exceptionCode.
   * 
   * @return the exceptionCode
   */
  public DhxExceptionEnum getExceptionCode() {
    return exceptionCode;
  }

  /**
   * Sets the exceptionCode.
   * 
   * @param exceptionCode the exceptionCode to set
   */
  public void setExceptionCode(DhxExceptionEnum exceptionCode) {
    this.exceptionCode = exceptionCode;
  }

}

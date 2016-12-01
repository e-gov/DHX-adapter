package ee.ria.dhx.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * DHX specific exception. contains error code.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class DhxException extends Exception {

  private DhxExceptionEnum exceptionCode;

  public DhxException(DhxExceptionEnum exceptionCode, String message) {
    super(message);
    this.setExceptionCode(exceptionCode);
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

}

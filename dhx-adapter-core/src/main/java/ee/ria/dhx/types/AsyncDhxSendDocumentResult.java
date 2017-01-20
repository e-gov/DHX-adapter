package ee.ria.dhx.types;

import java.util.Date;

public class AsyncDhxSendDocumentResult {

  public AsyncDhxSendDocumentResult(DhxSendDocumentResult result) {
    this.result = result;
    this.tryDate = new Date();
  }

  DhxSendDocumentResult result;
  Date tryDate;

  /**
   * Returns the result.
   * 
   * @return the result
   */
  public DhxSendDocumentResult getResult() {
    return result;
  }

  /**
   * Sets the result.
   * 
   * @param result the result to set
   */
  public void setResult(DhxSendDocumentResult result) {
    this.result = result;
  }

  /**
   * Return the tryDate.
   * 
   * @return the tryDate
   */
  public Date getTryDate() {
    return tryDate;
  }

  /**
   * Sets the tryDate.
   * 
   * @param tryDate the tryDate to set
   */
  public void setTryDate(Date tryDate) {
    this.tryDate = tryDate;
  }


}

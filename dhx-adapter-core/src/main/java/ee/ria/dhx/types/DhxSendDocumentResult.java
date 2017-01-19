package ee.ria.dhx.types;

import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;

/**
 * Represents result of package sending. Created when using asynchronous package sending for each
 * retry.
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxSendDocumentResult {

  public DhxSendDocumentResult(OutgoingDhxPackage sentPackage, SendDocumentResponse response) {
    this.sentPackage = sentPackage;
    this.response = response;
  }

  OutgoingDhxPackage sentPackage;
  SendDocumentResponse response;
  Exception occuredException;


  /**
   * Returns the sentPackage.
   * 
   * @return the sentPackage
   */
  public OutgoingDhxPackage getSentPackage() {
    return sentPackage;
  }

  /**
   * Sets the sentPackage.
   * 
   * @param sentPackage the sentPackage to set
   */
  public void setSentPackage(OutgoingDhxPackage sentPackage) {
    this.sentPackage = sentPackage;
  }

  /**
   * Returns the response.
   * 
   * @return the response of the service
   */
  public SendDocumentResponse getResponse() {
    return response;
  }

  /**
   * Sets the response.
   * 
   * @param response the response to set
   */
  public void setResponse(SendDocumentResponse response) {
    this.response = response;
  }

  /**
   * Returns the occuredException.
   * 
   * @return the Exception occured while sendind the document
   */
  public Exception getOccuredException() {
    return occuredException;
  }

  /**
   * Sets the occuredException.
   * 
   * @param occuredException the occuredException to set
   */
  public void setOccuredException(Exception occuredException) {
    this.occuredException = occuredException;
  }

}

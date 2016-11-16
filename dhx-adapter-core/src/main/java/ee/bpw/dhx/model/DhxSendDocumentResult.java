package ee.bpw.dhx.model;

import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents result package sending. Created when using asynchronous package sending for each
 * retry.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class DhxSendDocumentResult {

  public DhxSendDocumentResult(OutgoingDhxPackage sentPackage, SendDocumentResponse response) {
    this.sentPackage = sentPackage;
    this.response = response;
  }

  OutgoingDhxPackage sentPackage;
  SendDocumentResponse response;
  Exception occuredException;

}

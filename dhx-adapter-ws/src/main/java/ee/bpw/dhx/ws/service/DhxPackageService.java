package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxSendDocumentResult;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;

import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import org.springframework.ws.context.MessageContext;

import java.util.List;

/**
 * Interface for document sending and receiving. Service must be independent from capsule versions
 * that are being sent or received, that means that no changes should be done in service if new
 * capsule version is added.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DhxPackageService {

  /**
   * Send package. Package is sent to recipient defined in outgoingPackage.
   * 
   * @param outgoingPackage - package to send
   * @return - sendDocument service result
   * @throws DhxException - thrown if error occurs while sending document
   */
  public DhxSendDocumentResult sendPackage(OutgoingDhxPackage outgoingPackage)
      throws DhxException;

  /**
   * Send document. Every package is sent to recipient defined in it. If package sending gets
   * exception, then send document result with fault in it is created, sending will be continued
   * anyway.
   * 
   * @param outgoingPackages - package to send
   * @return - sendDocument service results
   * @throws DhxException - thrown if error occurs while sending document
   */
  public List<DhxSendDocumentResult> sendMultiplePackages(
      List<OutgoingDhxPackage> outgoingPackages) throws DhxException;

  /**
   * Method is used by endpoint. Is called when document arrives to endpoint.
   * 
   * @param document - service iniput parameters. document to receive
   * @param client - SOAP message client(who sent the request).
   * @param service - SOAP message service(to whom was request sent).
   * @param context - SOAP message context. If something is missing in parsed objects, then take
   *        them from context
   * @return service response
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public SendDocumentResponse receiveDocumentFromEndpoint(
      SendDocument document, InternalXroadMember client,
      InternalXroadMember service, MessageContext context)
      throws DhxException;
}

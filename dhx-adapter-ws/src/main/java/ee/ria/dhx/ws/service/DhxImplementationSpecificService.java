package ee.ria.dhx.ws.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;

import org.springframework.ws.context.MessageContext;

import java.util.List;

/**
 * Interface which declares methods that are needed for DHX web services to work, but those methods
 * are not implemented by DHX ws itself.
 * 
 * @author Aleksei Kokarev
 *
 */

public interface DhxImplementationSpecificService {

  /**
   * Method searches through saved documents and checks if document with same sender and consignment
   * id exists.
   * 
   * 
   * @param from - document sender to check
   * @param consignmentId - consignment id to check
   * @return - true if document with same sender and consignment id exists, otherwise false
   * @throws DhxException - thrown if error occurs
   */
  public abstract boolean isDuplicatePackage(InternalXroadMember from,
      String consignmentId) throws DhxException;

  /**
   * Method should receive document(save in database for example) and return unique id of it. Id
   * will be sent as receipt in response.
   * 
   * @param document - document to receive
   * @param context - if something is missing in document, then there is opportunity to take it from
   *        context
   * @return - unique id of the document that was saved.
   * @throws DhxException - thrown if error occurs while receiving document
   */
  public String receiveDocument(IncomingDhxPackage document,
      MessageContext context) throws DhxException;

  /**
   * Method returns list of representees.
   * 
   * @return List of representees that are represented by that X-road member or empty List if there
   *         are no representees.
   * @throws DhxException - thrown if error occurs
   */
  public abstract List<DhxRepresentee> getRepresentationList()
      throws DhxException;

  /**
   * Method returns adressees list from local storage(DB for example). Method does not renew
   * adressees list from X-road.
   * 
   * @return - adressees list from local storage
   * @throws DhxException - thrown if error occurs
   */
  public List<InternalXroadMember> getAdresseeList() throws DhxException;

  /**
   * Methods saves adressees list to local storage(DB for example). This method is called after
   * renewing adressees list from X-road.
   * 
   * @param members - list of the adressees to save
   * @throws DhxException - thrown if error occurs
   */
  public void saveAddresseeList(List<InternalXroadMember> members)
      throws DhxException;

  /**
   * If using asynchronous document sending. Then this method is a callback for receiving
   * sendDocument results. This method is used for both success and fail.
   * 
   * @param finalResult - last result of the package sending. Contains both package that was
   *        sent(including recipient and other information) and sending result, either success or
   *        fail. If sending was unsucessfull then final results response parameter will contain
   *        fault.
   * @param retryResults - contains information about all retries that were done. Meant for
   *        debugging or history saving or something like that.
   */
  public void saveSendResult(DhxSendDocumentResult finalResult,
      List<AsyncDhxSendDocumentResult> retryResults);

}

package ee.ria.dhx.types;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.util.CapsuleVersionEnum;

import java.io.File;
import java.io.InputStream;

import javax.activation.DataHandler;

public class OutgoingDhxPackage extends DhxPackage {

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param attachmentHandler - datahandler containing document being sent
   * @param internalConsignmentId - consingment id for sending document
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client, DataHandler attachmentHandler,
      String internalConsignmentId, String dhxProtocolVersion) throws DhxException {
    super(service, client, attachmentHandler, dhxProtocolVersion);
    this.internalConsignmentId = internalConsignmentId;

  }

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param stream - documents stream
   * @param internalConsignmentId - consingment id for sending document
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
 /* public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream,
      String internalConsignmentId, String dhxProtocolVersion)
      throws DhxException {
    super(service, client, stream, dhxProtocolVersion);
    this.internalConsignmentId = internalConsignmentId;
  }*/


  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param attachmentHandler - datahandler containing document being sent
   * @param internalConsignmentId - consingment id for sending document
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, DataHandler attachmentHandler, String internalConsignmentId,
      String dhxProtocolVersion)
      throws DhxException {
    super(service, client, parsedContainer, parsedContainerVersion, attachmentHandler, dhxProtocolVersion);
    this.internalConsignmentId = internalConsignmentId;
  }

  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param stream - stream of the document being sent
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param internalConsignmentId - consignment id for sending document
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while sending document
   */
 /* public OutgoingDhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, String internalConsignmentId,
      String dhxProtocolVersion)
      throws DhxException {
    super(service, client, stream, parsedContainer, parsedContainerVersion, dhxProtocolVersion);
    this.internalConsignmentId = internalConsignmentId;
  }
*/

  /**
   * internal id of the package(for package sending).
   */
  private String internalConsignmentId;

  /**
   * internal id of the package(for package sending).
   * 
   * @return internalConsignmentId - internal id of the package(for package sending).
   */
  public String getInternalConsignmentId() {
    return internalConsignmentId;
  }

  /**
   * internal id of the package(for package sending).
   * 
   * @param internalConsignmentId - internal id of the package(for package sending).
   */
  public void setInternalConsignmentId(String internalConsignmentId) {
    this.internalConsignmentId = internalConsignmentId;
  }


  @Override
  public String toString() {
    String objString = super.toString();
    if (getInternalConsignmentId() != null) {
      objString += "internalConsignmentId: " + getInternalConsignmentId();
    }
    return objString;
  }

}

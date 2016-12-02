package ee.ria.dhx.types;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.util.ByteArrayDataSource;

/**
 * DHX package object. Contains information needed for sending the document and for receiving the
 * document. Cannot be instantiated by itself. Eiteher outgoing or incoming package must be
 * instantiated
 * 
 * @author Aleksei Kokarev
 *
 */
public abstract class DhxPackage {

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param attachmentHandler - datahandler containing document being sent
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
  protected DhxPackage(InternalXroadMember service, InternalXroadMember client, DataHandler attachmentHandler,
      String dhxProtocolVersion)
      throws DhxException {
   // try {
     // InputStream stream = new FileInputStream(file);
     // DataSource source = new FileDataSource(file);
     // documentFile = new DataHandler(source);
      this.documentFile = attachmentHandler;
      this.service = service;
      this.client = client;
      this.dhxProtocolVersion = dhxProtocolVersion;
    /*} catch (FileNotFoundException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    } /*catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    }*/

  }

  /**
   * Create DhxDocument. For document sending
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param stream - documents stream
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while creating dhxdocument
   */
 /* protected DhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream, String dhxProtocolVersion)
      throws DhxException {
    try {
    //  InputStream realStream;
     // realStream = stream;
    //  DataSource source = new ByteArrayDataSource(realStream, "application/octet-stream");
    //  documentFile = new DataHandler(source);
      this.service = service;
      this.client = client;
      this.dhxProtocolVersion = dhxProtocolVersion;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR, ex.getMessage(), ex);
    }
  }*/


  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param attachmentHandler - datahandler containing document being sent
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while sending document
   */
  protected DhxPackage(InternalXroadMember service, InternalXroadMember client,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, DataHandler attachmentHandler, String dhxProtocolVersion)
      throws DhxException {
    this(service, client, attachmentHandler, dhxProtocolVersion);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
  }

  /**
   * Create DhxDocument. For document sending.
   * 
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param stream - stream of the document
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to that package. If
   *        package is being recieved, then dhxProtocolVersion is taken from request.
   * @throws DhxException - thrown if error occurs while sending document
   */
 /* protected DhxPackage(InternalXroadMember service, InternalXroadMember client,
      InputStream stream,
      Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion, String dhxProtocolVersion)
      throws DhxException {
    this(service, client, stream, dhxProtocolVersion);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
  }*/



  /**
   * Create DhxDocument. For document receiving
   * 
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param document - document to send
   */
  protected DhxPackage(InternalXroadMember client, InternalXroadMember service,
      SendDocument document) {
    this.documentFile = document.getDocumentAttachment();
    // this.externalConsignmentId = document.getConsignmentId();
    this.client = client;
    this.service = service;
    this.dhxProtocolVersion = document.getDHXVersion();
  }

  /**
   * Create DhxDocument. For document receiving.
   * 
   * @param client - XroadMember who sends the document(self X-road member for outgoing document)
   * @param service - XroadMember to whom document is sent(self X-road member for incoming document)
   * @param document - document to send
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   */
  protected DhxPackage(InternalXroadMember client, InternalXroadMember service,
      SendDocument document, Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion) {
    this(client, service, document);
    this.parsedContainer = parsedContainer;
    this.parsedContainerVersion = parsedContainerVersion;
  }

  // document file capsule
  private DataHandler documentFile;
  private InternalXroadMember client;
  private InternalXroadMember service;
  private String dhxProtocolVersion;



  private Object parsedContainer;

  private CapsuleVersionEnum parsedContainerVersion;

  /**
   * 
   * @return dhxProtocolVersion of the package being sent or received.
   */
  public String getDhxProtocolVersion() {
    return dhxProtocolVersion;
  }

  /**
   * 
   * @param dhxProtocolVersion - version of the DHX protocol which corresponds to the package.
   */
  public void setDhxProtocolVersion(String dhxProtocolVersion) {
    this.dhxProtocolVersion = dhxProtocolVersion;
  }

  /**
   * getDocumentFile.
   * 
   * @return - datahandler of the file being sent or received
   */
  public DataHandler getDocumentFile() {
    return documentFile;
  }

  /**
   * setDocumentFile.
   * 
   * @param documentFile - datahandler of the file being sent or received
   */
  public void setDocumentFile(DataHandler documentFile) {
    this.documentFile = documentFile;
  }

  /**
   * client represents the one who sent the document(document sender). if it is outbound document
   * then client represents self X-road member
   * 
   * @return client
   */
  public InternalXroadMember getClient() {
    return client;
  }

  /**
   * client represents the one who sent the document(document sender). if it is outbound document
   * then client is NULL.
   * 
   * @param client - client represents the one who sent the document(document sender). if it is
   *        outbound document then client represents self X-road member
   */
  public void setClient(InternalXroadMember client) {
    this.client = client;
  }

  /**
   * service represents the one to whom the document is being sent. if it is inbound document then
   * service represents self X-road member.
   * 
   * @return service
   */
  public InternalXroadMember getService() {
    return service;
  }

  /**
   * service represents the one to whom the document is being sent(adressee). if it is inbound
   * document then service represents self X-road member.
   * 
   * @param service - xroad member of the recipient
   */
  public void setService(InternalXroadMember service) {
    this.service = service;
  }



  /**
   * Parsed container of the document(capsule). Is of object type, because different capsule
   * versions might be sent with different object types. Container is parsed only if container
   * parsing is turned on in cofiguration, otherwise parsedConrtainer is NULL.
   * 
   * @return parsedContainer
   */
  public Object getParsedContainer() {
    return parsedContainer;
  }

  /**
   * Parsed container of the document(capsule). Is of object type, because different capsule
   * versions might be sent with different object types. Container is parsed only if container
   * parsing is turned on in cofiguration, otherwise parsedConrtainer is NULL.
   * 
   * @param parsedContainer - Parsed container of the document(capsule)
   */
  public void setParsedContainer(Object parsedContainer) {
    this.parsedContainer = parsedContainer;
  }

  /**
   * Version of the container that is parsed. Only filled if container parsing is turned on in
   * configuration, otherwise is NULL.
   * 
   * @return - parsed container object
   */
  public CapsuleVersionEnum getParsedContainerVersion() {
    return parsedContainerVersion;
  }

  /**
   * Version of the container that is parsed. Only filled if container parsing is turned on in
   * configuration, otherwise is NULL.
   * 
   * @param parsedContainerVersion - parsed container version
   */
  public void setParsedContainerVersion(CapsuleVersionEnum parsedContainerVersion) {
    this.parsedContainerVersion = parsedContainerVersion;
  }


  @Override
  public String toString() {
    String objString = "";
    if (getClient() != null) {
      objString += "client: " + getClient().toString();
    }
    if (getService() != null) {
      objString += " service: " + getClient().toString();
    }
    if (dhxProtocolVersion != null) {
      objString += " dhxProtocolVersion: " + dhxProtocolVersion;
    }
    if (getParsedContainerVersion() != null) {
      objString += " parsedContainerVersion: " + getParsedContainerVersion().toString();
    }
    return objString;
  }
}

package ee.ria.dhx.types;

import ee.ria.dhx.util.CapsuleVersionEnum;

import ee.ria.dhx.util.StringUtil;

import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;

public class IncomingDhxPackage extends DhxPackage {

  /**
   * Create DhxDocument. For document receiving
   * 
   * @param client - XroadMember from who the document is being sent
   * @param service - XroadMember who receives the document(self)
   * @param document - document to send
   */
  public IncomingDhxPackage(InternalXroadMember client, InternalXroadMember service,
      SendDocument document) {
    super(client, service, document);
    this.externalConsignmentId = document.getConsignmentId();
    this.recipient = getRecipient(document, service);
  }

  /**
   * Create DhxDocument. For document receiving.
   * 
   * @param client - XroadMember from who the document is being sent
   * @param document - document to send
   * @param service - XroadMember who receives the document(self)
   * @param parsedContainer - document Object. Object type bacause different version might be sent
   * @param parsedContainerVersion - version of the container
   */
  public IncomingDhxPackage(InternalXroadMember client, InternalXroadMember service,
      SendDocument document, Object parsedContainer,
      CapsuleVersionEnum parsedContainerVersion) {
    super(client, service, document, parsedContainer, parsedContainerVersion);
    this.externalConsignmentId = document.getConsignmentId();
    this.recipient = getRecipient(document, service);
  }

  /**
   * external ID of the package.(for package receiving)
   */
  private String externalConsignmentId;
  private DhxOrganisation recipient;



  /**
   * External ID of the package.(for package receiving).
   * 
   * @return externalConsignmentId
   */
  public String getExternalConsignmentId() {
    return externalConsignmentId;
  }

  /**
   * External ID of the package.(for package receiving).
   * 
   * @param externalConsignmentId - External ID of the package.(for package receiving).
   */
  public void setExternalConsignmentId(String externalConsignmentId) {
    this.externalConsignmentId = externalConsignmentId;
  }

  /**
   * 
   * @return - For incoming document. Contains recipient data. If document is sent to representee
   *         then object contains representees data, otherwise object contains direct recipient
   */
  public DhxOrganisation getRecipient() {
    return recipient;
  }

  private DhxOrganisation getRecipient(SendDocument document, InternalXroadMember service) {
    DhxOrganisation recipient = new DhxOrganisation();
    if (!StringUtil.isNullOrEmpty(document.getRecipient())) {
      recipient.setCode(document.getRecipient());
      recipient.setSystem(document.getRecipientSystem());
    } else {
      recipient.setCode(service.getMemberCode());
      recipient.setSystem(service.getSubsystemCode());
    }
    return recipient;
  }


  /**
   * 
   * @param recipient - For incoming document. Must contain recipient data(either representee or
   *        direct recipient)
   */
  public void setRecipient(DhxOrganisation recipient) {
    this.recipient = recipient;
  }


  @Override
  public String toString() {
    String objString = super.toString();
    if (getRecipient() != null) {
      objString += "recipient: " + getRecipient().toString();
    }
    if (getParsedContainerVersion() != null) {
      objString += "parsedContainerVersion: " + getParsedContainerVersion().toString();
    }
    return objString;
  }

}

package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.model.OutgoingDhxPackage;
import ee.bpw.dhx.util.CapsuleVersionEnum;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Provides Dhx packages to send using DHX protocol. Multiple methods to get outgoing packages are
 * provided. Besides the document that is being sent, package contains information about sender,
 * recipient and consignmentId.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DhxPackageProviderService {

  /**
   * Get outgoing package. Package recipient is defined in input. Default sender from configuration
   * will be set as X-road sender. No capsule parsing is done, only validation if it is enabled in
   * configuration. XSD schema of current capsule defined in configuration is used to validate
   * capsule. Protocol version from configuration is used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while creating package
   */
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, InternalXroadMember recipient)
      throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. X-road sender is put from sender
   * input parameter. Use this method if sender differs from the one configured as default(for
   * example if sending from non default subsystem or if representee is sending the document). No
   * capsule parsing is done, only validation if it is enabled in configuration. XSD schema of
   * current capsule defined in configuration is used to validate capsule. Protocol version from
   * configuration is used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @param sender - xroadMember representing sender of the document. Can be used if sender differs
   *        from the one from configuration or for example to send document from another(different
   *        from default configured) subsystem
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender) throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. Sender parameter will be put as
   * X-road sender. Use this method if sender differs from the one configured as default(for example
   * if sending from non default subsystem or if representee is sending the document). No capsule
   * parsing is done, only validation if it is enabled in configuration. XSD schema of current
   * capsule defined in configuration is used to validate capsule. Protocol version from
   * configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @param sender - xroadMember representing sender of the document. Can be used if sender differs
   *        from the one from configuration or for example to send document from another(different
   *        from default configured) subsystem
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender) throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. No capsule parsing is done, only
   * validation if it is enabled in configuration. XSD schema of current capsule defined in
   * configuration is used to validate capsule. Protocol version from configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient)
      throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. No capsule parsing is done, only
   * validation if it is enabled in configuration. XSD schema of current capsule defined in
   * configuration is used to validate capsule. Protocol version from configuration is used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem)
      throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. senderSubsystem represents X-road
   * subSystem which will used to send the document. Use this method if sending from same X-road
   * member as configured but from non default subsystem. No capsule parsing is done, only
   * validation if it is enabled in configuration. XSD schema of current capsule defined in
   * configuration is used to validate capsule. Protocol version from configuration is used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderSubsystem) throws DhxException;

  /**
   * Get outgoing package. Package recipient is defined in input. Sender is defined in input by
   * senderMemberCode and senderSubsystem. Use this method if sender differs from the configuder
   * default. For example if sending from behalf of representee. No capsule parsing is done, only
   * validation if it is enabled in configuration.XSD schema of current capsule defined in
   * configuration is used to validate capsule. Protocol version from configuration is used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderMemberCode - X-road member code of the sender to use when sending the document
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(File capsuleFile,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem)
      throws DhxException;

  /**
   * Get outgoing package. No capsule parsing is done and package recipient is defined in input.
   * Sender is defined in input by senderMemberCode and senderSubsystem. Use this method if sender
   * differs from the configuder default. For example if sending from behalf of representee. No
   * capsule parsing is done, only validation if it is enabled in configuration. XSD schema of
   * current capsule defined in configuration is used to validate capsule. Protocol version from
   * configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderMemberCode - X-road member code of the sender to use when sending the document
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem)
      throws DhxException;

  /**
   * Get outgoing package. No capsule parsing is done and package recipient is defined in input. No
   * capsule parsing is done, only validation if it is enabled in configuration. XSD schema of
   * current capsule defined in configuration is used to validate capsule. Protocol version from
   * configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem)
      throws DhxException;

  /**
   * Get outgoing package. No capsule parsing is done and package recipient is defined in input.
   * senderSubsystem represents X-road subSystem which will be used to send the document. Use this
   * method if sending from same X-road member as configured but from non default subsystem. No
   * capsule parsing is done, only validation if it is enabled in configuration. XSD schema of
   * current capsule defined in configuration is used to validate capsule. Protocol version from
   * configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderSubsystem) throws DhxException;

  /**
   * Get outgoing package. No capsule parsing is done and package recipient is defined in input.
   * Sender is defined in input by senderMemberCode and senderSubsystem. Use this method if sender
   * differs from the configuder default. For example if sending from behalf of representee. No
   * capsule parsing is done, only validation if it is enabled in configuration. XSD schema from
   * input is used for validation. Protocol version from input is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipientCode - to whom document is sent. registry code of the recipient or representee
   * @param recipientSystem - system of the recipient to send to. NULL if sending to default DHX
   *        system.
   * @param senderMemberCode - X-road member code of the sender to use when sending the document
   * @param senderSubsystem - X-road subsytem of the sender to use when sending the document
   * @param schemaStream - stream of the XSD schema to use for capsule validation.
   * @param dhxProtocolVersion - dhx protocol version to set to SOAP request that will be sent
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, String recipientCode, String recipientSystem,
      String senderMemberCode, String senderSubsystem,
      InputStream schemaStream, String dhxProtocolVersion)
      throws DhxException;

  /**
   * Get outgoing package. No capsule parsing is done and package recipient is defined in input.
   * Sender parameter will be put as X-road sender. Use this method if sender differs from the
   * configuder default. For example if sending from behalf of representee. No capsule parsing is
   * done, only validation if it is enabled in configuration. XSD schema from input is used for
   * validation. Protocol version from input is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param recipient - to whom the document is sent
   * @param sender - xroadMember representing sender of the document. Can be used if sender differs
   *        from the one from configuration or for example to send document from another(different
   *        from default configured) subsystem
   * @param schemaStream - stream of the XSD schema to use for capsule validation.
   * @param dhxProtocolVersion - dhx protocol version to set to SOAP request that will be sent
   * @return - OutgoingDhxPackage
   * @throws DhxException - thrown if error occurs while sending document
   */
  public OutgoingDhxPackage getOutgoingPackage(InputStream capsuleStream,
      String consignmentId, InternalXroadMember recipient,
      InternalXroadMember sender, InputStream schemaStream,
      String dhxProtocolVersion) throws DhxException;

  /**
   * Get outgoing package. Parses capsule from file and creates package for each adressee defined in
   * capsule. Uses default capsule version from configuration to parse capsule. No capsule parsing
   * is done, only validation if it is enabled in configuration. XSD schema of current capsule
   * defined in configuration is used to validate capsule. Protocol version from configuration is
   * used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @return - outgoing package list
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated it is not always possible to define recipient from capsule recipient. For example
   *             if multiple subsystems are present. Use methods with defined recipient in input
   */
  public List<OutgoingDhxPackage> getOutgoingPackage(File capsuleFile,
      String consignmentId) throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule. No capsule parsing
   * is done, only validation if it is enabled in configuration. XSD schema of current capsule
   * defined in configuration is used to validate capsule. Protocol version from configuration is
   * used.
   * 
   * @param capsuleFile - file to send
   * @param consignmentId - consignment id of the document
   * @param version - version of the capsule to parse
   * @return - outgoing package list
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated it is not always possible to define recipient from capsule recipient. For example
   *             if multiple subsystems are present. Use methods with defined recipient in input
   */
  public List<OutgoingDhxPackage> getOutgoingPackage(File capsuleFile,
      String consignmentId, CapsuleVersionEnum version)
      throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule. Protocol version
   * from configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @return - outgoing package list
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated it is not always possible to define recipient from capsule recipient. For example
   *             if multiple subsystems are present. Use methods with defined recipient in input
   */
  public List<OutgoingDhxPackage> getOutgoingPackage(
      InputStream capsuleStream, String consignmentId)
      throws DhxException;

  /**
   * Parses capsule from file and sends document. Document is sent to every recipient defined in
   * capsule. Uses default capsule version from configuration to parse capsule. Protocol version
   * from configuration is used.
   * 
   * @param capsuleStream - stream to send
   * @param consignmentId - consignment id of the document
   * @param version - version of the capsule to parse
   * @return - outgoing package list
   * @throws DhxException - thrown if error occurs while sending document
   * @deprecated it is not always possible to define recipient from capsule recipient. For example
   *             if multiple subsystems are present. Use methods with defined recipient in input
   */
  public List<OutgoingDhxPackage> getOutgoingPackage(
      InputStream capsuleStream, String consignmentId,
      CapsuleVersionEnum version) throws DhxException;

}

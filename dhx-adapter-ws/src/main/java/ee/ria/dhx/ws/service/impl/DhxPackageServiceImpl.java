package ee.ria.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.OutgoingDhxPackage;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for document sending and receiving. Service is independent from capsule versions that are
 * being sent or received, that means that no changes should be done in service if new capsule
 * version is added.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Service
public class DhxPackageServiceImpl implements DhxPackageService {

  @Autowired
  DhxConfig config;

  @Autowired
  SoapConfig soapConfig;

  @Autowired
  CapsuleConfig capsuleConfig;

  @Autowired
  AddressService addressService;

  @Autowired
  DhxGateway documentGateway;

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  @Autowired
  DhxImplementationSpecificService dhxImplementationSpecificService;

  private void checkProtocolVersion(String protocolVersion)
      throws DhxException {
    log.debug("checking protocol version " + protocolVersion);
    if (protocolVersion == null) {
      throw new DhxException(DhxExceptionEnum.PROTOCOL_VERSION_ERROR,
          "DHXVersion in empty.");
    }
    if (!config.getAcceptedDhxProtocolVersions().contains(
        "," + protocolVersion + ",")) {
      throw new DhxException(DhxExceptionEnum.PROTOCOL_VERSION_ERROR,
          "Version not supported.");
    }
  }

  /**
   * Method is used by endpoint. Is called when document arrives to endpoint Does capsule pasring if
   * it is configured.
   * 
   * @param document - service iniput parameters. document to receive
   * @param client - SOAP message client(who sent the request).
   * @return service response
   * @throws DhxException - thrown if error occurs while receiving document
   */
  @Override
  @Loggable
  public SendDocumentResponse receiveDocumentFromEndpoint(
      SendDocument document, InternalXroadMember client,
      InternalXroadMember service, MessageContext context)
      throws DhxException {
    if (StringUtil.isNullOrEmpty(document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DATA_ERROR, "Consignment id is empty!");
    }
    if (config.getCheckDuplicate()
        && dhxImplementationSpecificService.isDuplicatePackage(client,
            document.getConsignmentId())) {
      throw new DhxException(DhxExceptionEnum.DUPLICATE_PACKAGE,
          "Already got package with this consignmentID. from:"
              + client.toString() + " consignmentId:"
              + document.getConsignmentId());
    } else {
      if (document.getDocumentAttachment() == null) {
        throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
            "Attached capsule is not found in request");
      }
      if (!StringUtil.isNullOrEmpty(document.getRecipient())) {
        DhxRepresentee representee = new DhxRepresentee(document.getRecipient(), new Date(), null,
            null, document.getRecipientSystem());
        service.setRepresentee(representee);
      }
      IncomingDhxPackage dhxDocument;
      if (config.getParseCapsule()) {
        dhxDocument = extractAndValidateDocument(document, client,
            service);
      } else {
        dhxDocument = extractAndValidateDocumentNoParsing(document,
            client, service);
      }
      String id = dhxImplementationSpecificService.receiveDocument(
          dhxDocument, context);
      SendDocumentResponse response = new SendDocumentResponse();
      response.setReceiptId(id);
      return response;
    }
  }


  @Loggable
  @Override
  public DhxSendDocumentResult sendPackage(OutgoingDhxPackage outgoingPackage)
      throws DhxException {
    log.debug("Sending document.");
    SendDocumentResponse response = documentGateway
        .sendDocument(outgoingPackage);
    return new DhxSendDocumentResult(outgoingPackage, response);
  }

  @Loggable
  @Override
  public List<DhxSendDocumentResult> sendMultiplePackages(
      List<OutgoingDhxPackage> packages) {
    List<DhxSendDocumentResult> results = new ArrayList<DhxSendDocumentResult>();
    for (OutgoingDhxPackage outgoingPackage : packages) {
      results.add(sendDocumentTry(outgoingPackage));
    }
    return results;
  }

  /**
   * Tries to send document and if error occurs, then returns response with fault, not raises
   * exception.
   * 
   * @param document - Document to try to send
   * @return service response for single recipient defined in document
   */
  @Loggable
  protected DhxSendDocumentResult sendDocumentTry(OutgoingDhxPackage document) {
    SendDocumentResponse response = null;
    try {
      response = documentGateway.sendDocument(document);
    } catch (Exception ex) {
      log.error(
          "Error occured while sending docuemnt. " + ex.getMessage(),
          ex);
      DhxExceptionEnum faultCode = DhxExceptionEnum.TECHNICAL_ERROR;
      if (ex instanceof DhxException) {
        if (((DhxException) ex).getExceptionCode() != null) {
          faultCode = ((DhxException) ex).getExceptionCode();
        }
      }
      response = new SendDocumentResponse();
      Fault fault = new Fault();
      fault.setFaultCode(faultCode.getCodeForService());
      fault.setFaultString(ex.getMessage());
      response.setFault(fault);
    }
    return new DhxSendDocumentResult(document, response);
  }



  /**
   * Method extracts and validates capsule. Uses capsuleXsdFile21 configuration parameter for find
   * XSD against which to validate
   * 
   * @param document - SOAP request object
   * @param client - X-road member who did the SOAP query.
   * @param service - X-road member who owns the SOAP service
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - throws if error occured while reading or extracting file
   */
  @Loggable
  protected IncomingDhxPackage extractAndValidateDocument(
      SendDocument document, InternalXroadMember client,
      InternalXroadMember service) throws DhxException {
    InputStream schemaStream = null;
    try {
      log.info("Receiving document. for representative: {}",
          document.getRecipient());
      InputStream fileStream = document.getDocumentAttachment()
          .getInputStream();
      if (config.getCheckDhxVersion()) {
        checkProtocolVersion(document.getDHXVersion());
      }
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
      } else {
        log.debug("Validating capsule is disabled");
      }
      Object container = null;
      container = dhxMarshallerService.unmarshallAndValidate(fileStream,
          schemaStream);
      List<CapsuleAdressee> adressees = capsuleConfig
          .getAdresseesFromContainer(container);
      if (log.isDebugEnabled() && adressees != null) {
        for (CapsuleAdressee adressee : adressees) {
          log.debug(
              "Document data from capsule: recipient organisationCode: {}",
              adressee.getAdresseeCode());
        }
      }
      log.info("Document received.");
      DhxOrganisation recipient =
          DhxOrganisationFactory.createIncomingRecipientOrgnisation(document, service);
      IncomingDhxPackage dhxDocument = new IncomingDhxPackage(client,
          service, document, container,
          CapsuleVersionEnum.forClass(container.getClass()), recipient);
      if (config.getCheckRecipient()) {
        checkRecipient(dhxDocument.getRecipient(), adressees);
      }
      if (config.getCheckSender()) {
        checkSender(dhxDocument.getClient(),
            capsuleConfig.getSenderFromContainer(container));
      }
      log.debug("Recipients from capsule checked and found in representative list "
          + "or own member code. ");
      return dhxDocument;
    } catch (IOException ex) {
      throw new DhxException(
          DhxExceptionEnum.FILE_ERROR,
          "Error while getting attachment stream. " + ex.getMessage(),
          ex);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code: {} message: {}",
          ex.getExceptionCode(), ex.getMessage());
      throw ex;
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
  }

  /**
   * Method extracts and validates attached document. Attachment validation is not implemented in
   * this version of service.
   * 
   * @param document - SOAP request object
   * @param client - X-road member who did the SOAP query.
   * @param service - X-road member who owns the SOAP service
   * @return - parsed DHXDocument where there is attachment, recipient and attachment parsed XML if
   *         validation is enabled
   * @throws DhxException - thrown if error occurs while extracting or validating document
   */
  @Loggable
  protected IncomingDhxPackage extractAndValidateDocumentNoParsing(
      SendDocument document, InternalXroadMember client,
      InternalXroadMember service) throws DhxException {
    InputStream schemaStream = null;
    try {
      log.info("Receiving document. for representative: {}",
          document.getRecipient());
      if (config.getCheckDhxVersion()) {
        checkProtocolVersion(document.getDHXVersion());
      }
      if (config.getCapsuleValidate()) {
        log.debug("Validating capsule is enabled");
        schemaStream = FileUtil.getFileAsStream(capsuleConfig
            .getXsdForVersion(capsuleConfig
                .getCurrentCapsuleVersion()));
        dhxMarshallerService.validate(document.getDocumentAttachment()
            .getInputStream(), schemaStream);
      } else {
        log.debug("Validating capsule is disabled");
      }
      DhxOrganisation recipient =
          DhxOrganisationFactory.createIncomingRecipientOrgnisation(document, service);
      IncomingDhxPackage dhxDocument = new IncomingDhxPackage(client,
          service, document, recipient);
      if (config.getCheckRecipient()) {
        checkRecipient(dhxDocument.getRecipient(), null);
        log.info(
            "Recipient checked and found in representative list or own member code. recipient:"
                + document.getRecipient());
      }
      log.info("Document received.");
      return dhxDocument;
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          ex.getMessage());
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      log.info("Document is not received. code: {} message: {}",
          ex.getExceptionCode(), ex.getMessage());
      throw ex;
    } finally {
      FileUtil.safeCloseStream(schemaStream);
    }
  }

  /**
   * Checks if recipient is present in representativesList and in capsule recipients. Needed to be
   * sure if document is sent to right recipient. Recipient(if not null) MUST be found in list of
   * own memberCode or own representationList. Recipient or own member code MUST be found in
   * capsuleRecipients(if not null)
   * 
   * @param recipient - recipient from service input.(e.g. representee to whom document is sent or
   *        the direct recipient)
   * @param capsuleRecipients -recipient list parsed from capsule.
   * @throws DhxException throws if recipient not found. Means that document recipient if faulty
   */
  @Loggable
  protected void checkRecipient(DhxOrganisation recipient,
      List<CapsuleAdressee> capsuleRecipients) throws DhxException {
    log.info("Checking recipient.");
    List<DhxOrganisation> recipientList = new ArrayList<DhxOrganisation>();
    List<DhxRepresentee> representees = dhxImplementationSpecificService
        .getRepresentationList();
    Date curDate = new Date();
    if (representees != null && representees.size() > 0) {
      for (DhxRepresentee representee : representees) {
        if (representee.getStartDate().getTime() <= curDate.getTime()
            && (representee.getEndDate() == null || representee
                .getEndDate().getTime() >= curDate.getTime())) {
          recipientList.add(DhxOrganisationFactory.createDhxOrganisation(representee
              .getRepresenteeCode(), representee.getRepresenteeSystem()));
        }
      }
    }
    for (String subSystem : soapConfig.getAcceptedSubsystemsAsList()) {
      recipientList.add(DhxOrganisationFactory.createDhxOrganisation(soapConfig.getMemberCode(),
          subSystem));
    }
    Boolean found = false;
    for (DhxOrganisation rec : recipientList) {
      if (rec.equals(recipient)) {
        found = true;
        break;
      }
    }
    if (!found) {
      if (log.isDebugEnabled() && recipientList != null) {
        log.debug(
            "Recipient not found in representativesList and own member code. recipientList:");
        for (DhxOrganisation recipientFromList : recipientList) {
          log.debug("Recipient: " + recipientFromList.toString());
        }
      }
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in representativesList and own member code. recipient:"
              + recipient.toString());
    }
    if (capsuleRecipients != null) {
      for (CapsuleAdressee capsuleRecipient : capsuleRecipients) {
        if (recipient.equalsToCapsuleOrganisation(
            capsuleRecipient.getAdresseeCode())) {
          return;
        }
      }
      throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
          "Recipient not found in capsule recipient list. recipient:"
              + recipient);
    }
    return;
  }

  /**
   * Checks if sender is defined as capsule sender. Needed to be sure if document is sent from right
   * sender.
   * 
   * @param client - Xroad client from service input.(e.g. representee who sent document is sent or
   *        the direct sender)
   * @param capsuleSender -sender from capsule.
   * @throws DhxException throws if sender not valid. Means that document sender if faulty
   */
  @Loggable
  protected void checkSender(InternalXroadMember client,
      CapsuleAdressee capsuleSender) throws DhxException {
    log.info("Checking sender.");
    DhxOrganisation sender = DhxOrganisationFactory.createDhxOrganisation(client);
    if (client.getRepresentee() != null) {
      InternalXroadMember member = addressService.getClientForMemberCode(
          client.getMemberCode(), client.getSubsystemCode());
      if (member.getRepresentor() == null || member.getRepresentor() == false) {
        throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
            "Xroad sender is representee, but member found in "
                + "adressregistry is not representor. sender:"
                + sender);
      }
    }
    // check that capsule sender and Xroad sender are the same
    if (sender.equalsToCapsuleOrganisation(capsuleSender.getAdresseeCode())) {
      return;
    }
    throw new DhxException(DhxExceptionEnum.WRONG_SENDER,
        "Xroad sender not found in capsule. sender:" + sender);
  }

  /**
   * Sets the config.
   * 
   * @param config the config to set
   */
  public void setConfig(DhxConfig config) {
    this.config = config;
  }

  /**
   * Sets the soapConfig.
   * 
   * @param soapConfig the soapConfig to set
   */
  public void setSoapConfig(SoapConfig soapConfig) {
    this.soapConfig = soapConfig;
  }

  /**
   * Sets the capsuleConfig.
   * 
   * @param capsuleConfig the capsuleConfig to set
   */
  public void setCapsuleConfig(CapsuleConfig capsuleConfig) {
    this.capsuleConfig = capsuleConfig;
  }

  /**
   * Sets the addressService.
   * 
   * @param addressService the addressService to set
   */
  public void setAddressService(AddressService addressService) {
    this.addressService = addressService;
  }

  /**
   * Sets the documentGateway.
   * 
   * @param documentGateway the documentGateway to set
   */
  public void setDocumentGateway(DhxGateway documentGateway) {
    this.documentGateway = documentGateway;
  }

  /**
   * Sets the dhxMarshallerService.
   * 
   * @param dhxMarshallerService the dhxMarshallerService to set
   */
  public void setDhxMarshallerService(DhxMarshallerService dhxMarshallerService) {
    this.dhxMarshallerService = dhxMarshallerService;
  }

  /**
   * Sets the dhxImplementationSpecificService.
   * 
   * @param dhxImplementationSpecificService the dhxImplementationSpecificService to set
   */
  public void setDhxImplementationSpecificService(
      DhxImplementationSpecificService dhxImplementationSpecificService) {
    this.dhxImplementationSpecificService = dhxImplementationSpecificService;
  }


}

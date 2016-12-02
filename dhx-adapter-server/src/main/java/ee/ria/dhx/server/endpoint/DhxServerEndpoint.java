package ee.ria.dhx.server.endpoint;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.service.DhxDocumentService;
import ee.ria.dhx.server.service.util.AttachmentUtil;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.impl.DhxGateway;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedV3RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.soap.SoapMessage;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

@Slf4j
@Endpoint
/**
 * Endpoint class which offers SOAP services.
 * @author Aleksei Kokarev
 *
 */
public class DhxServerEndpoint {

  public static final String NAMESPACE_URI = "http://producers.dhl.xrd.riik.ee/producer/dhl";



  @Autowired
  DhxGateway dhxGateway;

  @Autowired
  DhxDocumentService dhxDocumentService;

  /**
   * X-road SOAP service sendDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocuments")
  @ResponsePayload
  @Loggable
  public SendDocumentsResponse sendDocuments(@RequestPayload SendDocuments request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if(!service.getServiceVersion().equals("v4")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Only v4 version of sendDocuments is supported");
    }
    if (log.isDebugEnabled()) {
      log.debug("Got sendDocument request from: " + client.toString());
      log.debug("Got sendDocument request to: " + service.toString());
    }
    SendDocumentsResponse response = dhxDocumentService.sendDocuments(request, client, service);
    return response;
  }

  /**
   * X-road SOAP service receiveDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "receiveDocuments")
  @ResponsePayload
  @Loggable
  public ReceiveDocumentsResponse receiveDocuments(
      @RequestPayload ReceiveDocuments request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if(!service.getServiceVersion().equals("v4")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Only v4 version of receiveDocuments is supported");
    }
    ReceiveDocumentsResponse response =
        dhxDocumentService.receiveDocuments(request, client, service);
    return response;
  }

  /**
   * X-road SOAP service markDocumentsReceived.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "markDocumentsReceived")
  @ResponsePayload
  @Loggable
  public MarkDocumentsReceivedResponse markDocumentsReceived(
      @RequestPayload MarkDocumentsReceived request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if(!service.getServiceVersion().equals("v3")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Only v3 version of markDocumentsReceived is supported");
    }
    MarkDocumentsReceivedResponse response =
        dhxDocumentService.markDocumentReceived(request.getKeha(), client, service);
    return response;
  }

  /**
   * X-road SOAP service getSendStatus.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getSendStatus")
  @ResponsePayload
  @Loggable
  public GetSendStatusResponse getSendStatus(@RequestPayload GetSendStatus request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if(!service.getServiceVersion().equals("v2")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Only v2 version of getSendStatus is supported");
    }
    return dhxDocumentService.getSendStatus(request, client, service);
  }

  /**
   * X-road SOAP service sendDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getSendingOptions")
  @ResponsePayload
  @Loggable
  public GetSendingOptionsResponse getSendingOptions(@RequestPayload GetSendingOptions request,
      MessageContext messageContext) throws DhxException {
    GetSendingOptionsResponse response = new GetSendingOptionsResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
      /*
       * if (log.isDebugEnabled()) { log.debug("Got sendDocument request from: " +
       * client.toString()); }
       */
      /*
       * response = documentService.receiveDocumentFromEndpoint(request, client, service,
       * messageContext);
       */
      // TODO: add to DB useing service
    } catch (DhxException ex) {
      // log.error(ex.getMessage(), ex);
      if (ex.getExceptionCode().isBusinessException()) {
        Fault fault = new Fault();
        fault.setFaultCode(ex.getExceptionCode().getCodeForService());
        fault.setFaultString(ex.getMessage());
        // response.setFault(fault);
      } else {
        throw ex;
      }
    }
    return response;
  }

}

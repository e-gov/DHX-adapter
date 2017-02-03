package ee.ria.dhx.server.endpoint;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.service.SoapService;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.server.types.ee.riik.schemas.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.service.impl.DhxGateway;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Slf4j
@Endpoint
/**
 * Endpoint class which offers SOAP services.
 * 
 * @author Aleksei Kokarev
 *
 */
public class ServerEndpoint {

  public static final String NAMESPACE_URI = "http://producers.dhl.xrd.riik.ee/producer/dhl";



  @Autowired
  DhxGateway dhxGateway;

  @Autowired
  SoapService soapService;

  @Autowired
  Jaxb2Marshaller marsh;

  /**
   * X-road SOAP service sendDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains ID of the saved document
   * @throws DhxException - thrown if error occurred while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocuments")
  @ResponsePayload
  @Loggable
  public SendDocumentsResponse sendDocuments(@RequestPayload SendDocuments request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if (!service.getServiceVersion().equals("v4")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Only v4 version of sendDocuments is supported");
    }
    if (log.isDebugEnabled()) {
      log.debug("Got sendDocument request from: {}", client);
      log.debug("Got sendDocument request to: {}", service);
    }
    if (request.getKeha().getDokumendid().getHref() == null) {
      request.getKeha().getDokumendid().setHref(WsUtil.extractAttachment(messageContext,
          request.getKeha().getDokumendid().getHrefString()));
    }
    SendDocumentsResponse response = soapService.sendDocuments(request, client, service);
    String contentId = WsUtil.addAttachment(messageContext, response.getKeha().getHref());
    response.getKeha().setHrefString(contentId);
    return response;
  }



  /**
   * X-road SOAP service receiveDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains documents to receive
   * @throws DhxException - thrown if error occurred while receiving the documents
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "receiveDocuments")
  @ResponsePayload
  @Loggable
  public ReceiveDocumentsResponse receiveDocuments(
      @RequestPayload ReceiveDocuments request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if (!service.getServiceVersion().equals("v4")
        && !service.getServiceVersion().equals("v3")
        && !service.getServiceVersion().equals("v2")
        && !service.getServiceVersion().equals("v1")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Only v1,v2,v3,v4 versions of receiveDocuments are supported");
    }
    ReceiveDocumentsResponse response =
        soapService.receiveDocuments(request, client, service);
    String contentId = WsUtil.addAttachment(messageContext, response.getKeha().getHref());
    response.getKeha().setHrefString(contentId);
    return response;
  }

  /**
   * X-road SOAP service markDocumentsReceived.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains confirmation of marking the documents as received
   * @throws DhxException - thrown if error occurred while marking documents received
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "markDocumentsReceived")
  @ResponsePayload
  @Loggable
  public MarkDocumentsReceivedResponse markDocumentsReceived(
      @RequestPayload MarkDocumentsReceived request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if (!service.getServiceVersion().equals("v3")
        && !service.getServiceVersion().equals("v2")
        && !service.getServiceVersion().equals("v1")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Only v1,v2,v3 versions of markDocumentsReceived are supported");
    }
    MarkDocumentsReceivedResponse response =
        soapService.markDocumentReceived(request, client, service, messageContext);
    return response;
  }

  /**
   * X-road SOAP service getSendStatus.
   * 
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains statuses of the documents
   * @throws DhxException - thrown if error occurred while getting send statuses
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getSendStatus")
  @ResponsePayload
  @Loggable
  public GetSendStatusResponse getSendStatus(@RequestPayload GetSendStatus request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    if (!service.getServiceVersion().equals("v2")
        && !service.getServiceVersion().equals("v1")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Only v1,v2 versions of getSendStatus are supported");
    }
    GetSendStatusResponse response =
        soapService.getSendStatus(request, client, service, messageContext);
    String contentId = WsUtil.addAttachment(messageContext, response.getKeha().getHref());
    response.getKeha().setHrefString(contentId);
    return response;
  }

  /**
   * X-road SOAP service getSendingOptions.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains sending options(list of organisations)
   * @throws DhxException - thrown if error occurred while getting sending options
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getSendingOptions")
  @ResponsePayload
  @Loggable
  public GetSendingOptionsResponse getSendingOptions(@RequestPayload GetSendingOptions request,
      MessageContext messageContext) throws DhxException {
    InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
    InternalXroadMember service = dhxGateway.getXroadService(messageContext);
    // set default version
    if (service.getServiceVersion() == null) {
      service.setServiceVersion("v3");
    }
    if (!service.getServiceVersion().equals("v3")
        && !service.getServiceVersion().equals("v2")
        && !service.getServiceVersion().equals("v1")) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Only v1,v2,v3 versions of getSendingOptions are supported");
    }
    GetSendingOptionsResponse response =
        soapService.getSendingOptions(request, client, service, messageContext);
    return response;
  }

}

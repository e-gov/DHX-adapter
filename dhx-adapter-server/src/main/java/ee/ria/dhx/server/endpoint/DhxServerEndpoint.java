package ee.ria.dhx.server.endpoint;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.service.util.AttachmentUtil;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.impl.DhxGateway;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatus;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptions;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendingOptionsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceived;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.MarkDocumentsReceivedResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsResponse;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4RequestType;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsResponse;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;

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
  DhxPackageService documentService;

  @Autowired
  DhxImplementationSpecificService dhxImplementationSpecificService;

  @Autowired
  DhxGateway dhxGateway;

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
    SendDocumentsResponse response = new SendDocumentsResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
      if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
        log.debug("Got sendDocument request to: " + service.toString());
      }
      try{
       InputStream attachment = /*AttachmentUtil.base64decodeAndUnzip(*/request.getKeha().getDokumendid().getHref().getInputStream()/*)*/;
       int ch;
       StringBuilder sb = new StringBuilder();
       while((ch = attachment.read()) != -1)
           sb.append((char)ch);
       log.info("Got attachemnt:" +  sb.toString());
      }catch(IOException ex) {
        log.error("Error ocured." + ex.getMessage(), ex);
      }
      /*response =
          documentService.receiveDocumentFromEndpoint(request, client, service, messageContext);*/
      //TODO: add to DB useing service
    } catch (DhxException ex) {
    //  log.error(ex.getMessage(), ex);
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


  /**
   * X-road SOAP service sendDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "receiveDocuments")
  @ResponsePayload
  @Loggable
  public ReceiveDocumentsResponse receiveDocuments(@RequestPayload ReceiveDocumentsV4RequestType request,
      MessageContext messageContext) throws DhxException {
    ReceiveDocumentsResponse response = new ReceiveDocumentsResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
     /* if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
      }*/
      /*response =
          documentService.receiveDocumentFromEndpoint(request, client, service, messageContext);*/
      //TODO: add to DB useing service
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
  
  /**
   * X-road SOAP service sendDocuments.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "markDocumentsReceived")
  @ResponsePayload
  @Loggable
  public MarkDocumentsReceivedResponse markDocumentsReceived(@RequestPayload MarkDocumentsReceived request,
      MessageContext messageContext) throws DhxException {
    MarkDocumentsReceivedResponse response = new MarkDocumentsReceivedResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
     /* if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
      }*/
      /*response =
          documentService.receiveDocumentFromEndpoint(request, client, service, messageContext);*/
      //TODO: add to DB useing service
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
  
  /**
   * X-road SOAP service sendDocuments.
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
    GetSendStatusResponse response = new GetSendStatusResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
     /* if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
      }*/
      /*response =
          documentService.receiveDocumentFromEndpoint(request, client, service, messageContext);*/
      //TODO: add to DB useing service
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
     /* if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
      }*/
      /*response =
          documentService.receiveDocumentFromEndpoint(request, client, service, messageContext);*/
      //TODO: add to DB useing service
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

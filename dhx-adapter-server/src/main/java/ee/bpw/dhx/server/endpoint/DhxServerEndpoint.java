package ee.bpw.dhx.server.endpoint;

import com.jcabi.aspects.Loggable;


import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.ws.service.DhxImplementationSpecificService;
import ee.bpw.dhx.ws.service.DhxPackageService;
import ee.bpw.dhx.ws.service.impl.DhxGateway;
import ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4RequestType;
import ee.riik.xrd.dhl.producers.producer.dhl.ReceiveDocumentsV4ResponseType;
import ee.riik.xrd.dhl.producers.producer.dhl.SendDocuments;
import ee.riik.xrd.dhl.producers.producer.dhl.SendDocumentsV4RequestType;

import eu.x_road.dhx.producer.Fault;
import eu.x_road.dhx.producer.RepresentationList;
import eu.x_road.dhx.producer.RepresentationListResponse;
import eu.x_road.dhx.producer.Representees;
import eu.x_road.dhx.producer.SendDocument;
import eu.x_road.dhx.producer.SendDocumentResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

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
  public SendDocumentResponse sendDocuments(@RequestPayload SendDocuments request,
      MessageContext messageContext) throws DhxException {
    SendDocumentResponse response = new SendDocumentResponse();
    try {
      InternalXroadMember client = dhxGateway.getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway.getXroadService(messageContext);
      if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
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
  public ReceiveDocumentsV4ResponseType receiveDocuments(@RequestPayload ReceiveDocumentsV4RequestType request,
      MessageContext messageContext) throws DhxException {
    ReceiveDocumentsV4ResponseType response = new ReceiveDocumentsV4ResponseType();
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

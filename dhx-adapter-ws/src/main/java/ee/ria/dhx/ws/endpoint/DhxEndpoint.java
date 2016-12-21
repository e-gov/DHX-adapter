package ee.ria.dhx.ws.endpoint;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Fault;
import ee.ria.dhx.types.eu.x_road.dhx.producer.RepresentationList;
import ee.ria.dhx.types.eu.x_road.dhx.producer.RepresentationListResponse;
import ee.ria.dhx.types.eu.x_road.dhx.producer.Representees;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.impl.DhxGateway;

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
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxEndpoint {

  public static final String NAMESPACE_URI = "http://dhx.x-road.eu/producer";

  @Autowired
  DhxPackageService documentService;

  @Autowired
  DhxImplementationSpecificService dhxImplementationSpecificService;

  @Autowired
  DhxGateway dhxGateway;

  /**
   * X-road SOAP service sendDocument.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains information about document being received or not
   * @throws DhxException - thrown if error occured while sending document
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "sendDocument")
  @ResponsePayload
  @Loggable
  public SendDocumentResponse sendDocument(
      @RequestPayload SendDocument request, MessageContext messageContext)
      throws DhxException {
    SendDocumentResponse response = new SendDocumentResponse();
    try {
      InternalXroadMember client = dhxGateway
          .getXroadClientAndSetRersponseHeader(messageContext);
      InternalXroadMember service = dhxGateway
          .getXroadService(messageContext);
      if (log.isDebugEnabled()) {
        log.debug("Got sendDocument request from: " + client.toString());
      }
      response = documentService.receiveDocumentFromEndpoint(request,
          client, service, messageContext);
    } catch (DhxException ex) {
      log.error(ex.getMessage(), ex);
      if (ex.getExceptionCode().isBusinessException()) {
        Fault fault = new Fault();
        fault.setFaultCode(ex.getExceptionCode().getCodeForService());
        fault.setFaultString(ex.getMessage());
        response.setFault(fault);
      } else {
        throw ex;
      }
    }
    return response;
  }

  /**
   * X-road SOAP service representationList.
   * 
   * @param request - service request
   * @param messageContext - SOAP message context
   * @return - service response. contains representee list
   * @throws DhxException - throws when error occurs while getting representation list
   */
  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "representationList")
  @ResponsePayload
  @Loggable
  public RepresentationListResponse representationList(
      @RequestPayload RepresentationList request,
      MessageContext messageContext) throws DhxException {
    try {
      RepresentationListResponse response = new RepresentationListResponse();
      InternalXroadMember client = dhxGateway
          .getXroadClientAndSetRersponseHeader(messageContext);
      if (log.isDebugEnabled()) {
        log.debug("Got representationList request from: "
            + client.toString());
      }
      List<DhxRepresentee> internalRepresentees = dhxImplementationSpecificService
          .getRepresentationList();
      if (internalRepresentees != null) {
        Representees representees = new Representees();
        for (DhxRepresentee internalRepresentee : internalRepresentees) {
          representees.getRepresentee().add(
              internalRepresentee.convertToRepresentee());
        }
        response.setRepresentees(representees);
      }
      return response;
    } catch (Exception ex) {
      log.error(ex.getMessage(), ex);
      throw ex;
    }
  }

}

package ee.bpw.dhx.ws;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.DhxRepresentee;
import ee.bpw.dhx.model.InternalXroadMember;
import ee.bpw.dhx.ws.service.DhxMarshallerService;

import eu.x_road.xsd.identifiers.ObjectFactory;
import eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import eu.x_road.xsd.representation.XRoadRepresentedPartyType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.http.HttpTransportConstants;
import org.springframework.xml.transform.StringSource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;

import javax.xml.bind.JAXBElement;
import javax.xml.soap.AttachmentPart;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

/**
 * class to set header when sending SOAP message.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class SoapRequestHeaderModifier implements WebServiceMessageCallback {

  private InternalXroadMember service;
  private InternalXroadMember client;
  private String serviceName;
  private String serviceVersion;
  private String protocolVersion;
  private DhxMarshallerService dhxMarshallerService;

  /**
   * SoapRequestHeaderModifier constructor. 
   * @param service service
   * @param client client
   * @param serviceName servicename
   * @param serviceVersion serviceVersion
   * @param protocolVersion protocolVersion
   */
  public SoapRequestHeaderModifier(InternalXroadMember service,
      InternalXroadMember client, String serviceName,
      String serviceVersion, String protocolVersion) {
    super();
    this.service = service;
    this.serviceName = serviceName;
    this.serviceVersion = serviceVersion;
    this.client = client;
    this.protocolVersion = protocolVersion;
    dhxMarshallerService = (DhxMarshallerService) AppContext
        .getApplicationContext().getBean("dhxMarshallerService");
  }

  @SuppressWarnings({"rawtypes"})
  @Override
  public void doWithMessage(WebServiceMessage message) throws IOException,
      TransformerException {
    try {
      SoapHeader header = ((SoapMessage) message).getSoapHeader();
      for (Iterator it = ((SaajSoapMessage) message).getSaajMessage()
          .getAttachments(); it.hasNext();) {
        AttachmentPart attachment = (AttachmentPart) it.next();
        log.debug("attachment part: {}", attachment.getContentType());
        attachment
            .setMimeHeader(
                HttpTransportConstants.HEADER_CONTENT_TRANSFER_ENCODING,
                "base64");
      }
      TransformerFactory fact = TransformerFactory.newInstance();
      Transformer transformer = fact.newTransformer();
      eu.x_road.xsd.xroad.ObjectFactory factory = new eu.x_road.xsd.xroad.ObjectFactory();

      transformer.transform(marshallObject(factory
          .createProtocolVersion(protocolVersion)), header
          .getResult());
      transformer.transform(marshallObject(factory.createId(UUID
          .randomUUID().toString())), header.getResult());
      transformer.transform(marshallObject(factory
          .createClient(getXRoadClientIdentifierType())), header
          .getResult());
      if (client.getRepresentee() != null) {
        transformer
            .transform(marshallObject(getRepresented(client
                .getRepresentee())), header.getResult());
      }
      transformer.transform(marshallObject(factory
          .createService(getXRoadServiceIdentifierType())), header
          .getResult());

    } catch (DhxException ex) {
      throw new RuntimeException(ex);
    }
  }

  private JAXBElement<XRoadRepresentedPartyType> getRepresented(
      DhxRepresentee rpresentee) {
    eu.x_road.xsd.representation.ObjectFactory factory =
        new eu.x_road.xsd.representation.ObjectFactory();
    XRoadRepresentedPartyType party = new XRoadRepresentedPartyType();
    party.setPartyCode(rpresentee.getMemberCode());
    return factory.createRepresentedParty(party);
  }

  private StringSource marshallObject(Object obejct) throws DhxException {
    String result = "";
    StringWriter sw = dhxMarshallerService.marshallToWriter(obejct);
    result = sw.toString();
    return new StringSource(result);
  }

  private XRoadClientIdentifierType getXRoadClientIdentifierType() {
    ObjectFactory factory = new ObjectFactory();
    XRoadClientIdentifierType clientXroad = factory
        .createXRoadClientIdentifierType();
    clientXroad.setXRoadInstance(client.getXroadInstance());
    clientXroad.setMemberClass(client.getMemberClass());
    clientXroad.setMemberCode(client.getMemberCode());
    clientXroad.setSubsystemCode(client.getSubsystemCode());
    return clientXroad;
  }

  private XRoadServiceIdentifierType getXRoadServiceIdentifierType() {
    ObjectFactory factory = new ObjectFactory();
    XRoadServiceIdentifierType service = factory
        .createXRoadServiceIdentifierType();
    service.setXRoadInstance(this.service.getXroadInstance());
    service.setMemberClass(this.service.getMemberClass());
    service.setSubsystemCode(this.service.getSubsystemCode());
    service.setMemberCode(this.service.getMemberCode());
    service.setServiceCode(serviceName);
    service.setServiceVersion(serviceVersion);
    service.setObjectType(XRoadObjectType.SERVICE);
    return service;
  }
}

package ee.ria.dhx.server.integration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadClientIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;
import ee.ria.dhx.types.eu.x_road.xsd.representation.ObjectFactory;
import ee.ria.dhx.types.eu.x_road.xsd.representation.XRoadRepresentedPartyType;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.context.AppContext;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationTestHelper {

  public static XRoadClientIdentifierType getClient(String memberCode) {
    XRoadClientIdentifierType client = new XRoadClientIdentifierType();
    client.setMemberClass("GOV");
    client.setMemberCode(memberCode);
    client.setSubsystemCode("DHX");
    client.setXRoadInstance("ee-dev");

    return client;
  }

  public static XRoadServiceIdentifierType getService(String memberCode, String serviceVersion) {
    XRoadServiceIdentifierType service = new XRoadServiceIdentifierType();
    service.setMemberClass("GOV");
    service.setMemberCode(memberCode);
    service.setSubsystemCode("DHX");
    service.setXRoadInstance("ee-dev");
    service.setServiceVersion(serviceVersion);

    return service;
  }

  public static XRoadRepresentedPartyType getRepresentee(String memberCode) {
    XRoadRepresentedPartyType representee = new XRoadRepresentedPartyType();
    representee.setPartyCode(memberCode);
    return representee;
  }

  public static Source getEnvelope(XRoadClientIdentifierType client,
      XRoadServiceIdentifierType service,
      XRoadRepresentedPartyType representee, Object body) throws DhxException {
    try {
      DhxConfig config = AppContext.getApplicationContext().getBean(DhxConfig.class);
      Marshaller marshaller = config.getJaxbContext().createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      String envelopePre =
          "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
              + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
              + " <soapenv:Header>";
      String envelope2 = " </soapenv:Header> <soapenv:Body>";
      String envelopePost = "</soapenv:Body></soapenv:Envelope>";
      StringWriter writer = new StringWriter();
      writer.write(envelopePre);
      ee.ria.dhx.types.eu.x_road.xsd.xroad.ObjectFactory factory =
          new ee.ria.dhx.types.eu.x_road.xsd.xroad.ObjectFactory();
      ee.ria.dhx.types.eu.x_road.xsd.representation.ObjectFactory reprFactory =
          new ObjectFactory();
      marshaller.marshal(factory.createClient(client), writer);
      marshaller.marshal(factory.createService(service), writer);
      if (representee != null) {
        marshaller.marshal(reprFactory.createRepresentedParty(representee), writer);
      }
      writer.write(envelope2);
      marshaller.marshal(body, writer);
      writer.write(envelopePost);
      log.debug("created envelope:" + writer.toString());
      StringReader reader = new StringReader(writer.toString());
      StreamSource source = new StreamSource(reader);
      return source;
    } catch (JAXBException ex) {
      throw new DhxException("Error", ex);
    }
  }

  public static DataHandler createDatahandlerFromList(List<? extends Object> objList,
      Boolean onlyGzip)
      throws DhxException {
    try {
      DhxConfig config = AppContext.getApplicationContext().getBean(DhxConfig.class);
      Marshaller marshaller = config.getJaxbContext().createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      FileOutputStream fos = null;
      GZIPOutputStream zippedStream = null;
      OutputStream base64Stream = null;
      try {
        File file = FileUtil.createPipelineFile();
        fos = new FileOutputStream(file);
        if (onlyGzip) {
          zippedStream = WsUtil.getGZipCompressStream(fos);
        } else {
          base64Stream = WsUtil.getBase64EncodeStream(fos);
          zippedStream = WsUtil.getGZipCompressStream(base64Stream);
        }
        for (Object obj : objList) {
          marshaller.marshal(obj, zippedStream);
        }
        zippedStream.finish();
        if (base64Stream != null) {
          base64Stream.flush();
        }
        fos.flush();
        DataSource datasource = new FileDataSource(file);
        return new DataHandler(datasource);
      } catch (IOException ex) {
        throw new DhxException(DhxExceptionEnum.FILE_ERROR,
            "Error occured while creating attachment for response. " + ex.getMessage(), ex);
      } finally {
        FileUtil.safeCloseStream(base64Stream);
        FileUtil.safeCloseStream(zippedStream);
        FileUtil.safeCloseStream(fos);
      }
    } catch (JAXBException ex) {
      throw new DhxException("Error", ex);
    }
  }



}

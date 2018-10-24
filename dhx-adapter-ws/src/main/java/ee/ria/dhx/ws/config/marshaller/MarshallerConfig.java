package ee.ria.dhx.ws.config.marshaller;

import ee.ria.dhx.ws.config.DhxConfig;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import javax.activation.DataHandler;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.transform.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;

/**
 * Contains beans needed for marshalling.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Configuration
public class MarshallerConfig {

  @Autowired
  DhxConfig dhxConfig;

  /**
   * initializes using configured marshall context if needed and returns
   * Jaxb2Marshaller.
   * 
   * @return Jaxb2Marshaller
   */
  @Bean(name = "dhxJaxb2Marshaller")
  public Jaxb2Marshaller getDhxJaxb2Marshaller() {
    DhxJaxb2Marshaller dhxJaxb2Marshaller = null;
    dhxJaxb2Marshaller = new DhxJaxb2Marshaller();
    // dhxJaxb2Marshaller.setMtomEnabled(true);
    log.debug("Creating marshaller for folowing paths: " + dhxConfig.getMarshallContext());
    dhxJaxb2Marshaller.setContextPaths(dhxConfig.getMarshallContextAsList());
    return dhxJaxb2Marshaller;
  }

  private static class DhxJaxb2Marshaller extends Jaxb2Marshaller {

    // Marshalling

    @Override
    public void marshal(Object graph, Result result) throws XmlMappingException {
      marshal(graph, result, null);
    }

    @Override
    public void marshal(Object graph, Result result, MimeContainer mimeContainer) throws XmlMappingException {
      try {
        Marshaller marshaller = createMarshaller();
        if (mimeContainer != null) {
          marshaller.setAttachmentMarshaller(new DhxJaxb2AttachmentMarshaller(mimeContainer));
        }
        marshaller.marshal(graph, result);
      } catch (JAXBException ex) {
        throw convertJaxbException(ex);
      }
    }

    private static class DhxJaxb2AttachmentMarshaller extends AttachmentMarshaller {

      private final MimeContainer mimeContainer;

      public DhxJaxb2AttachmentMarshaller(MimeContainer mimeContainer) {
        this.mimeContainer = mimeContainer;
      }

      @Override
      public String addMtomAttachment(byte[] data, int offset, int length, String mimeType, String elementNamespace,
          String elementLocalName) {
        return "";
      }

      @Override
      public String addMtomAttachment(DataHandler dataHandler, String elementNamespace, String elementLocalName) {
        return "";
      }

      @Override
      public String addSwaRefAttachment(DataHandler dataHandler) {
        // log.debug("DhxJaxb2AttachmentMarshaller.addSwaRefAttachment");
        String contentId = UUID.randomUUID() + "@" + dataHandler.getName();
        this.mimeContainer.addAttachment(contentId, dataHandler);
        return contentId;
      }

      @Override
      public boolean isXOPPackage() {
        return false;
      }
    }
  }

}

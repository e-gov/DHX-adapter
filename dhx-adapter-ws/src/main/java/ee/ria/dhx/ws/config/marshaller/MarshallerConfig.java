package ee.ria.dhx.ws.config.marshaller;

import ee.ria.dhx.ws.config.DhxConfig;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.UnmarshallingFailureException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.mime.MimeContainer;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.xml.StaxUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


//import org.apache.axiom.om.OMException;
//import org.apache.axiom.om.OMXMLStreamReader;
//import javax.xml.stream.XMLStreamReader;



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
   * initializes using configured marshall context if needed and returns Jaxb2Marshaller.
   * 
   * @return Jaxb2Marshaller
   */
  @Bean(name = "dhxJaxb2Marshaller")
  public Jaxb2Marshaller getDhxJaxb2Marshaller() {
    DhxJaxb2Marshaller dhxJaxb2Marshaller = null;
    dhxJaxb2Marshaller = new DhxJaxb2Marshaller();
    //dhxJaxb2Marshaller.setMtomEnabled(true);
    log.debug("Creating marshaller for folowing paths: "
        + dhxConfig.getMarshallContext());
    dhxJaxb2Marshaller.setContextPaths(dhxConfig.getMarshallContextAsList());
    dhxJaxb2Marshaller.setMtomEnabled(true);
    return dhxJaxb2Marshaller;
  }

  
  
  private static class DhxJaxb2Marshaller extends Jaxb2Marshaller {
  
    private static final String CID = "cid:";
    
    private Class<?> mappedClass;
    
    public void setMappedClass(Class<?> mappedClass) {
      this.mappedClass = mappedClass;
      
      super.setMappedClass(mappedClass);
  }
    
    // Marshalling

    @Override
    public void marshal(Object graph, Result result) throws XmlMappingException {
        marshal(graph, result, null);
    }

    @Override
    public void marshal(Object graph, Result result, MimeContainer mimeContainer) throws XmlMappingException {
        try {
            Marshaller marshaller = createMarshaller();
            //if (this.mtomEnabled && mimeContainer != null) {
            if (mimeContainer != null) {
                marshaller.setAttachmentMarshaller(new DhxJaxb2AttachmentMarshaller(mimeContainer));
            }
            if (StaxUtils.isStaxResult(result)) {
                marshalStaxResult(marshaller, graph, result);
            }
            else {
                marshaller.marshal(graph, result);
            }
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    private void marshalStaxResult(Marshaller jaxbMarshaller, Object graph, Result staxResult) throws JAXBException {
        XMLStreamWriter streamWriter = StaxUtils.getXMLStreamWriter(staxResult);
        if (streamWriter != null) {
            jaxbMarshaller.marshal(graph, streamWriter);
        }
        else {
            XMLEventWriter eventWriter = StaxUtils.getXMLEventWriter(staxResult);
            if (eventWriter != null) {
                jaxbMarshaller.marshal(graph, eventWriter);
            }
            else {
                throw new IllegalArgumentException("StAX Result contains neither XMLStreamWriter nor XMLEventConsumer");
            }
        }
    }    
    
    
    // Unmarshalling

    @Override
    public Object unmarshal(Source source) throws XmlMappingException {
        return unmarshal(source, null);
    }

    @Override
    public Object unmarshal(Source source, MimeContainer mimeContainer) throws XmlMappingException {
        source = processSource(source);

        try {
            Unmarshaller unmarshaller = createUnmarshaller();
            //if (this.mtomEnabled && mimeContainer != null) {
            if (mimeContainer != null) {
                unmarshaller.setAttachmentUnmarshaller(new DhxJaxb2AttachmentUnmarshaller(mimeContainer));
            }
            if (StaxUtils.isStaxSource(source)) {
                return unmarshalStaxSource(unmarshaller, source);
            }
            else if (this.mappedClass != null) {
                return unmarshaller.unmarshal(source, this.mappedClass).getValue();
            }
            else {
                return unmarshaller.unmarshal(source);
            }
        }
        catch (NullPointerException ex) {
            if (!isSupportDtd()) {
                throw new UnmarshallingFailureException("NPE while unmarshalling. " +
                        "This can happen on JDK 1.6 due to the presence of DTD " +
                        "declarations, which are disabled.", ex);
            }
            throw ex;
        }
        catch (JAXBException ex) {
            throw convertJaxbException(ex);
        }
    }

    protected Object unmarshalStaxSource(Unmarshaller jaxbUnmarshaller, Source staxSource) throws JAXBException {
        XMLStreamReader streamReader = StaxUtils.getXMLStreamReader(staxSource);
        if (streamReader != null) {
            return (this.mappedClass != null ?
                    jaxbUnmarshaller.unmarshal(streamReader, this.mappedClass).getValue() :
                    jaxbUnmarshaller.unmarshal(streamReader));
        }
        else {
            XMLEventReader eventReader = StaxUtils.getXMLEventReader(staxSource);
            if (eventReader != null) {
                return (this.mappedClass != null ?
                        jaxbUnmarshaller.unmarshal(eventReader, this.mappedClass).getValue() :
                        jaxbUnmarshaller.unmarshal(eventReader));
            }
            else {
                throw new IllegalArgumentException("StaxSource contains neither XMLStreamReader nor XMLEventReader");
            }
        }
    }
    
    
    private Source processSource(Source source) {
      if (StaxUtils.isStaxSource(source) || source instanceof DOMSource) {
          return source;
      }

      XMLReader xmlReader = null;
      InputSource inputSource = null;

      if (source instanceof SAXSource) {
          SAXSource saxSource = (SAXSource) source;
          xmlReader = saxSource.getXMLReader();
          inputSource = saxSource.getInputSource();
      }
      else if (source instanceof StreamSource) {
          StreamSource streamSource = (StreamSource) source;
          if (streamSource.getInputStream() != null) {
              inputSource = new InputSource(streamSource.getInputStream());
          }
          else if (streamSource.getReader() != null) {
              inputSource = new InputSource(streamSource.getReader());
          }
          else {
              inputSource = new InputSource(streamSource.getSystemId());
          }
      }

      try {
          if (xmlReader == null) {
              xmlReader = XMLReaderFactory.createXMLReader();
          }
          xmlReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", !isSupportDtd());
          String name = "http://xml.org/sax/features/external-general-entities";
          xmlReader.setFeature(name, isProcessExternalEntities());
          if (!isProcessExternalEntities()) {
              xmlReader.setEntityResolver(NO_OP_ENTITY_RESOLVER);
          }
          return new SAXSource(xmlReader, inputSource);
      }
      catch (SAXException ex) {
          logger.warn("Processing of external entities could not be disabled", ex);
          return source;
      }
    }
    
    private static class DhxJaxb2AttachmentMarshaller extends AttachmentMarshaller {

      private final MimeContainer mimeContainer;

      public DhxJaxb2AttachmentMarshaller(MimeContainer mimeContainer) {
          this.mimeContainer = mimeContainer;
      }

      @Override
      public String addMtomAttachment(byte[] data, int offset, int length, String mimeType,
              String elementNamespace, String elementLocalName) {
          //log.debug("DhxJaxb2AttachmentMarshaller.addMtomAttachment1");
          ByteArrayDataSource dataSource = new ByteArrayDataSource(mimeType, data, offset, length);
          return addMtomAttachment(new DataHandler(dataSource), elementNamespace, elementLocalName);
      }

      @Override
      public String addMtomAttachment(DataHandler dataHandler, String elementNamespace, String elementLocalName) {
          //log.debug("DhxJaxb2AttachmentMarshaller.addMtomAttachment2");
          String host = getHost(elementNamespace, dataHandler);
          String contentId = UUID.randomUUID() + "@" + host;
          this.mimeContainer.addAttachment("<" + contentId + ">", dataHandler);
          try {
              contentId = URLEncoder.encode(contentId, "UTF-8");
          }
          catch (UnsupportedEncodingException ex) {
              // ignore
          }
          return CID + contentId;
      }

      private String getHost(String elementNamespace, DataHandler dataHandler) {
          try {
              URI uri = new URI(elementNamespace);
              return uri.getHost();
          }
          catch (URISyntaxException ex) {
              // ignore
          }
          return dataHandler.getName();
      }

      @Override
      public String addSwaRefAttachment(DataHandler dataHandler) {
          String contentId = UUID.randomUUID() + "@" + dataHandler.getName();
          this.mimeContainer.addAttachment(contentId, dataHandler);
          log.debug("DhxJaxb2AttachmentMarshaller.addSwaRefAttachment contentId={}", contentId);
          return contentId;
      }

      @Override
      public boolean isXOPPackage() {
          return false; //this.mimeContainer.convertToXopPackage();
      }
  }

    private static class DhxJaxb2AttachmentUnmarshaller extends AttachmentUnmarshaller {

      private final MimeContainer mimeContainer;

      public DhxJaxb2AttachmentUnmarshaller(MimeContainer mimeContainer) {
          this.mimeContainer = mimeContainer;
      }

      @Override
      public byte[] getAttachmentAsByteArray(String cid) {
          try {
              DataHandler dataHandler = getAttachmentAsDataHandler(cid);
              return FileCopyUtils.copyToByteArray(dataHandler.getInputStream());
          }
          catch (IOException ex) {
              throw new UnmarshallingFailureException("Couldn't read attachment", ex);
          }
      }

      @Override
      public DataHandler getAttachmentAsDataHandler(String contentId) {
          if (contentId.startsWith(CID)) {
              contentId = contentId.substring(CID.length());
              try {
                  contentId = URLDecoder.decode(contentId, "UTF-8");
              }
              catch (UnsupportedEncodingException ex) {
                  // ignore
              }
              contentId = '<' + contentId + '>';
          }
          return this.mimeContainer.getAttachment(contentId);
      }

      @Override
      public boolean isXOPPackage() {
          return false; //this.mimeContainer.isXopPackage();
      }
  }
    
    
    /**
     * DataSource that wraps around a byte array.
     */
    private static class ByteArrayDataSource implements DataSource {

        private final byte[] data;

        private final String contentType;

        private final int offset;

        private final int length;

        public ByteArrayDataSource(String contentType, byte[] data, int offset, int length) {
            this.contentType = contentType;
            this.data = data;
            this.offset = offset;
            this.length = length;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(this.data, this.offset, this.length);
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContentType() {
            return this.contentType;
        }

        @Override
        public String getName() {
            return "ByteArrayDataSource";
        }
    }

    private static final EntityResolver NO_OP_ENTITY_RESOLVER = new EntityResolver() {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) {
          return new InputSource(new StringReader(""));
      }
    };
    
  }
  
  
  
  
}

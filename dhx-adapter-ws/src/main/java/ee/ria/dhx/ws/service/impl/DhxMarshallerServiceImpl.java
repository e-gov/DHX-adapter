package ee.ria.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

@Slf4j
@Service("dhxMarshallerService")
public class DhxMarshallerServiceImpl implements DhxMarshallerService {

  JAXBContext jaxbContext;

  @Autowired
  @Setter
  DhxConfig config;

  @Override
  public Unmarshaller getUnmarshaller() throws DhxException {
    try {
      return jaxbContext.createUnmarshaller();
    } catch (JAXBException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while creating unmarshaller. "
              + ex.getMessage(),
          ex);
    }
  }

  @Override
  public Marshaller getMarshaller() throws DhxException {
    try {
      return jaxbContext.createMarshaller();
    } catch (JAXBException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while creating marshaller. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Postconstruct init method. Sets marshallers needed for that service.
   * 
   * @throws JAXBException - thrown when error occured
   */
  @PostConstruct
  public void init() throws JAXBException {
    jaxbContext = config.getJaxbContext();
    // marshaller = jaxbMarshaller.getJaxbContext().createMarshaller();
    // unmarshaller = jaxbMarshaller.getJaxbContext().createUnmarshaller();
  }

  /**
   * Method parses(unmarshalls) object.
   * 
   * @param source - source of the marshalled object
   * @return - unmarshalled object
   * @throws DhxException - thrown if error occurs while unmrashalling object
   */
  @SuppressWarnings("unchecked")
  @Loggable
  public <T> T unmarshall(Source source) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      Object obj = getUnmarshaller().unmarshal(source);
      return (T) obj;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleFile - file to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @SuppressWarnings("unchecked")
  @Loggable
  public <T> T unmarshall(File capsuleFile) throws DhxException {
    try {
      log.debug("Unmarshalling file: {}", capsuleFile.getAbsolutePath());
      return (T) unmarshall(new FileInputStream(capsuleFile));
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param capsuleStream - stream to parse
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshall(final InputStream capsuleStream)
      throws DhxException {

    return unmarshallAndValidate(capsuleStream, null);
  }

  /**
   * Parses(unmarshalls) object from file. And does validation against XSD schema if schemaStream is
   * present.
   * 
   * @param capsuleStream - stream of to parse
   * @param schemaStream - stream on XSD schema against which to validate. No validation is done if
   *        stream is NULL
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshallAndValidate(final InputStream capsuleStream,
      InputStream schemaStream) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      Unmarshaller unmarshaller = getUnmarshaller();
      setSchemaForUnmarshaller(schemaStream, unmarshaller);
      return unmarshallNoValidation(capsuleStream, unmarshaller);
    } finally {
      // wont set single schema for unmarshaller
      // unmarshaller.setSchema(null);
    }
  }

  /**
   * Parses(unmarshalls) object from file. And does validation against XSD schema if schemaStream is
   * present.
   * 
   * @param capsuleFile - file to parse
   * @param schemaStream - stream on XSD schema against which to validate. No validation is done if
   *        stream is NULL
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshallAndValidate(File capsuleFile,
      InputStream schemaStream) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      Unmarshaller unmarshaller = getUnmarshaller();
      setSchemaForUnmarshaller(schemaStream, unmarshaller);
      return unmarshallNoValidation(new FileInputStream(capsuleFile), unmarshaller);
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    } finally {
      // wont set single schema for unmarshaller
      // unmarshaller.setSchema(null);
    }
  }

  @SuppressWarnings("unchecked")
  @Loggable
  protected <T> T unmarshallNoValidation(final InputStream capsuleStream,
      Unmarshaller unmarshaller) throws DhxException {
    try {
      Object obj = (Object) unmarshaller.unmarshal(capsuleStream);
      return (T) obj;
    } catch (JAXBException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  @Loggable
  private void setSchemaForUnmarshaller(InputStream schemaStream,
      Unmarshaller unmarshaller) throws DhxException {
    try {
      if (schemaStream != null) {
        Source schemaSource = new StreamSource(schemaStream);
        SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaSource);
        unmarshaller.setSchema(schema);
      }
    } catch (SAXException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while setting schema for unmarshaller. "
              + ex.getMessage(),
          ex);
    }
  }

  @Loggable
  private void setSchemaForMarshaller(InputStream schemaStream,
      Marshaller marshaller) throws DhxException {
    try {
      if (schemaStream != null) {
        Source schemaSource = new StreamSource(schemaStream);
        SchemaFactory schemaFactory = SchemaFactory
            .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(schemaSource);
        marshaller.setSchema(schema);
      }
    } catch (SAXException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while setting schema for unmarshaller. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to file.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public File marshall(Object container) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      File outputFile = FileUtil.createPipelineFile();
      getMarshaller().marshal(container, outputFile);

      return outputFile;
    } catch (IOException | JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to outputStream.
   * 
   * @param container - object to marshall
   * @param stream - containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  @Override
  public void marshallToOutputStream(Object container, OutputStream stream) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      getMarshaller().marshal(container, stream);
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to outputStream, removes all namespace prefixes from XML.
   * 
   * @param container - object to marshall
   * @param stream - containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  @Override
  public void marshallToOutputStreamNoNamespacePrefixes(Object container, OutputStream stream)
      throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
      XMLStreamWriter writer = outputFactory.createXMLStreamWriter(stream);
      writer.setNamespaceContext(new NamespaceContext() {
        @SuppressWarnings("rawtypes")
        public Iterator getPrefixes(String namespaceURI) {
          return null;
        }

        public String getPrefix(String namespaceURI) {
          return "";
        }

        public String getNamespaceURI(String prefix) {
          return null;
        }
      });
      getMarshaller().marshal(container, writer);
    } catch (JAXBException | XMLStreamException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to result.
   * 
   * @param obj - object to marshall
   * @param result - result into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public void marshallToResult(Object obj, Result result) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling object");
      }
      getMarshaller().marshal(obj, result);
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to node.
   * 
   * @param obj - object to marshall
   * @param node - node into which object will be marshalled
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public void marshallToNode(Object obj, Node node) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling object");
      }
      getMarshaller().marshal(obj, node);
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to writer.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public StringWriter marshallToWriter(Object container) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      StringWriter writer = new StringWriter();
      getMarshaller().marshal(container, writer);
      return writer;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to writer and validates object if schemaStream is present.
   * 
   * @param container object to marshall
   * @param schemaStream {@link InputStream} of the XSD schema to validate against
   * @return - file containing marshalled object
   * @throws DhxException thrown if error occurs while marshalling object
   */
  public StringWriter marshallToWriterAndValidate(Object container, InputStream schemaStream)
      throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      Marshaller marshaller = getMarshaller();
      setSchemaForMarshaller(schemaStream, marshaller);
      StringWriter writer = new StringWriter();
      marshaller.marshal(container, writer);
      return writer;
    } catch (JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Method validates file against XSD schema.
   * 
   * @param file - file to validate
   * @param schemaStream - stream caontaining XSD schema
   * @throws DhxException - thrown if error occurs
   */
  @Loggable
  public void validate(File file, InputStream schemaStream)
      throws DhxException {
    validate(FileUtil.getFileAsStream(file), schemaStream);
  }

  /**
   * Function validates file against XSD schema.
   * 
   * @param fileStream - stream to validate
   * @param schemaStream - stream containing schema against which to validate
   * @throws DhxException - thrown if file is not validated against XSD schema.
   */
  @Loggable
  public void validate(final InputStream fileStream, InputStream schemaStream)
      throws DhxException {
    try {
      log.info("Starting validating document capsule.");
      Source schemaSource = new StreamSource(schemaStream);
      // to prevent original inpustream closing crete a new one
      Source xmlFile = new StreamSource(fileStream);
      SchemaFactory schemaFactory = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(schemaSource);
      Validator validator = schema.newValidator();
      validator.validate(xmlFile);
      log.info("Document capsule is validated.");
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while validating capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Returns the jaxbContext.
   * 
   * @return the jaxbContext
   */
  public JAXBContext getJaxbContext() {
    return jaxbContext;
  }

}

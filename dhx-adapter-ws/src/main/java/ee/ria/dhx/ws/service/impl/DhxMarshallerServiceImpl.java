package ee.ria.dhx.ws.service.impl;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.bigdata.BigDataMarshallHandler;
import ee.ria.dhx.bigdata.BigDataXmlReader;
import ee.ria.dhx.bigdata.ReflectionUtil;
import ee.ria.dhx.bigdata.annotation.BigDataXmlElement;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.xml.serialize.XMLSerializer;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;
import org.codehaus.stax2.validation.XMLValidationException;
import org.codehaus.stax2.validation.XMLValidationSchema;
import org.codehaus.stax2.validation.XMLValidationSchemaFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Version DhxMarshallerService for capsules with big data files. No limitation on data file size is
 * set, data files from capsule are written to filesystem.
 * 
 * <p>
 * In order for big data logic to work, object parameter which might be BIG must be annotated with
 * {@link BigDataXmlElement} annotation and be of type {@link File}. {@link XMLSerializer} is used
 * during marshalling/unmarshalling process and does all the job except for the parameters annotated
 * with {@link BigDataXmlElement}.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Service("dhxMarshallerService")
public class DhxMarshallerServiceImpl implements DhxMarshallerService {

  JAXBContext jaxbContext;

  @Autowired
  @Setter
  DhxConfig config;

  @Autowired
  @Setter
  CapsuleConfig capsuleConfig;

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
   * Parses(unmarshalls) object from file.
   *
   * @param <T> - type of the capsule being unmarshalled
   * @param capsuleFile file to parse
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @return parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @SuppressWarnings("unchecked")
  @Loggable
  public <T> T unmarshall(File capsuleFile, Class<? extends Object> bigDataClass)
      throws DhxException {
    try {
      log.debug("Unmarshalling file: {}", capsuleFile.getAbsolutePath());
      return (T) unmarshall(new FileInputStream(capsuleFile), bigDataClass);
    } catch (FileNotFoundException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }
  
  /**
   * Parses(unmarshalls) object from Node.
   * 
   * @param <T> - type of the capsule being unmarshalled
   * @param node - node to unmarshall
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @SuppressWarnings("unchecked")
  public <T> T unmarshall(Node node)
      throws DhxException {
    try {
      return (T) getUnmarshaller().unmarshal(node);
    } catch (JAXBException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while unmarshalling node.", ex);
    }
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
    if (log.isDebugEnabled()) {
      log.debug("unmarshalling file");
    }
    Unmarshaller unmarshaller = getUnmarshaller();
    setSchemaForUnmarshaller(schemaStream, unmarshaller);
    return unmarshallNoValidation(capsuleStream, unmarshaller);
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
    }
  }
  
  /**
   * Parses(unmarshalls) object from file. And does validation against XSD schema if schemaStream is
   * present.
   * 
   * @param <T> - type of the capsule being unmarshalled
   * @param capsuleStream - stream of to parse
   * @param schemaStream - stream on XSD schema against which to validate. No validation is done if
   *        stream is NULL
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshallAndValidate(final InputStream capsuleStream,
      InputStream schemaStream, Class<? extends Object> bigDataClass) throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("unmarshalling file");
      }
      Unmarshaller unmarshaller = getUnmarshaller();
      setSchemaForUnmarshaller(schemaStream, unmarshaller);
      return unmarshallNoValidation(capsuleStream, unmarshaller, bigDataClass);
    } finally {
      // wont set single schema for unmarshaller
      // unmarshaller.setSchema(null);
    }
  }

  @Loggable
  protected <T> T unmarshallNoValidation(final InputStream capsuleStream,
      Unmarshaller unmarshaller) throws DhxException {
    return unmarshallNoValidation(capsuleStream, unmarshaller,
        capsuleConfig.getCurrentCapsuleVersion().getContainerClass());
  }

  @SuppressWarnings("unchecked")
  @Loggable
  protected <T> T unmarshallNoValidation(final InputStream capsuleStream,
      Unmarshaller unmarshaller, Class<? extends Object> bigDataClass) throws DhxException {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setNamespaceAware(true);
      SAXParser parser = factory.newSAXParser();
      XMLReader xmlReader = parser.getXMLReader();

      BigDataXmlReader bigDataXmlReader = new BigDataXmlReader(xmlReader, bigDataClass);
      SAXSource source = new SAXSource(bigDataXmlReader, new InputSource(capsuleStream));
      Object obj = (Object) unmarshaller.unmarshal(source);
      log.debug("Found big data elements: " + bigDataXmlReader.getBigDataElements().size());
      ReflectionUtil.setBigDataFieldsToObject(bigDataXmlReader.getBigDataElements(), obj);
      return (T) obj;
    } catch (SAXException | JAXBException | ParserConfigurationException ex) {
      log.error(ex.getMessage(), ex);

      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object with BIG DATA from capsule. " + ex.getMessage(),
          ex);
    }
  }


  /**
   * Parses(unmarshalls) object from file.
   * 
   * @param <T> - type of the capsule being unmarshalled
   * @param capsuleStream - stream to parse
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @return - parsed(unmarshalled) object
   * @throws DhxException - thrown if error occurs while parsing file
   */
  @Loggable
  public <T> T unmarshall(final InputStream capsuleStream, Class<? extends Object> bigDataClass)
      throws DhxException {

    return unmarshallAndValidate(capsuleStream, null, bigDataClass);
  }


  @Loggable
  protected void setSchemaForUnmarshaller(InputStream schemaStream,
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
  protected void setSchemaForMarshaller(InputStream schemaStream,
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
      marshall(container, outputFile);

      return outputFile;
    } catch (IOException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. "
              + ex.getMessage(),
          ex);
    }
  }

  /**
   * Marshalls object to given file.
   * 
   * @param container object to marshall
   * @param file file to marshall to
   * @throws DhxException thrown if error occurs while marshalling object
   */
  @Loggable
  @Override
  public void marshall(Object container, File file) throws DhxException {
    BufferedOutputStream bufStream = null;
    FileOutputStream stream = null;
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container");
      }
      stream = new FileOutputStream(file);
      bufStream = new BufferedOutputStream(stream);
      getMarshaller().marshal(container,
          new BigDataMarshallHandler(capsuleConfig.getCurrentCapsuleVersion().getContainerClass(),
              container, bufStream));
      bufStream.flush();
      stream.flush();
    } catch (IOException | JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(stream);
      FileUtil.safeCloseStream(bufStream);
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
   * @param includeXmlns if true, then xmlns will be added
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  @Override
  public void marshallToOutputStreamNoNamespacePrefixes(Object container, OutputStream stream, Boolean includeXmlns)
      throws DhxException {
    try {
      if (log.isDebugEnabled()) {
        log.debug("marshalling container no namespaces");
      }
      Marshaller marshaller = getMarshaller();
    //  marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      marshaller.marshal(container,
          new BigDataMarshallHandler(capsuleConfig.getCurrentCapsuleVersion().getContainerClass(),
              container, stream, true, includeXmlns));
    } catch (IOException | JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
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
   * Marshalls object to writer.
   * 
   * @param container - object to marshall
   * @return - file containing marshalled object
   * @throws DhxException - thrown if error occurs while marshalling object
   */
  @Loggable
  public StringWriter marshallToWriter(Object container) throws DhxException {
    return marshallToWriterAndValidate(container, null);
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
        log.debug("marshalling container no namespaces");
      }
      StringWriter writer = new StringWriter();
      Marshaller marshaller = getMarshaller();
      setSchemaForMarshaller(schemaStream, marshaller);
      marshaller.marshal(container,
          new BigDataMarshallHandler(capsuleConfig.getCurrentCapsuleVersion().getContainerClass(),
              container, writer));
      return writer;
    } catch (IOException | JAXBException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
          "Error occured while creating object from capsule. " + ex.getMessage(), ex);
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
      // to prevent original inpustream closing crete a new one
      XMLValidationSchemaFactory schemaFactory = XMLValidationSchemaFactory.newInstance(XMLValidationSchema.SCHEMA_ID_W3C_SCHEMA);
      XMLValidationSchema validationSchema = schemaFactory.createSchema(schemaStream);
      XMLStreamReader2 xmlStreamReader = (XMLStreamReader2) XMLInputFactory.newInstance().createXMLStreamReader(fileStream);
      xmlStreamReader.validateAgainst(validationSchema);

      try {
        while (xmlStreamReader.hasNext()) {
          xmlStreamReader.next();
        }
      } finally {
        xmlStreamReader.close();
      }

      log.info("Document capsule is validated.");
    } catch (XMLValidationException ex) {
      throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
              "Error occurred while validating capsule. " + ex.getMessage(), ex);
    } catch (Exception ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occurred while preparing for capsule validation. " + ex.getMessage(), ex);
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

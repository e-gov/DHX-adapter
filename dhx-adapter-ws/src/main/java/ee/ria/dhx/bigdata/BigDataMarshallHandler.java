package ee.ria.dhx.bigdata;

import com.jcabi.aspects.Loggable;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.CharBuffer;
import java.util.Stack;

/**
 * BIG DATA content handler specific for object marshalling.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class BigDataMarshallHandler extends BigDataHandler {


  private Object objectToMarshall;

  private OutputStream stream;

  private Boolean nonamespaces = false;
  
  private Boolean includeXmlns = false;



  /**
   * BigDataMarshallHandler constructor.
   * 
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @param objectToMarshall object to be marshalled
   * @param stream stream to write marshalled object to
   * @throws IOException thrown if error occurs
   */
  public BigDataMarshallHandler(Class<? extends Object> bigDataClass, Object objectToMarshall,
      OutputStream stream) throws IOException {
    super(bigDataClass);
    XMLSerializer serializer = new XMLSerializer();
    serializer.setOutputByteStream(stream);
    super.setHandler(serializer.asContentHandler());
    this.objectToMarshall = objectToMarshall;
    this.stream = stream;
  }
  
  /**
   * BigDataMarshallHandler constructor.
   * 
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @param objectToMarshall object to be marshalled
   * @param writer writer to write marshalled object to
   * @throws IOException thrown if error occurs
   */
  public BigDataMarshallHandler(Class<? extends Object> bigDataClass, Object objectToMarshall,
      Writer writer) throws IOException {
    super(bigDataClass);
    XMLSerializer serializer = new XMLSerializer();
    serializer.setOutputCharStream(writer);
    super.setHandler(serializer.asContentHandler());
    this.objectToMarshall = objectToMarshall;
  }

  /**
   * BigDataMarshallHandler constructor.
   * 
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @param objectToMarshall object to be marshalled
   * @param stream stream to write marshalled object to
   * @param nonamespaces is marshaller should remove namespaces
   * @param includeXmlns if true, then xmlns will be added
   * @throws IOException thrown if error occurs
   */
  public BigDataMarshallHandler(Class<? extends Object> bigDataClass, Object objectToMarshall,
      OutputStream stream, Boolean nonamespaces, Boolean includeXmlns) throws IOException {
    super(bigDataClass);

    XMLSerializer serializer = null;
    if (nonamespaces) {
      OutputFormat format = new OutputFormat();
      format.setOmitXMLDeclaration(true);
      serializer = new XMLSerializer(format);
    } else {
      serializer = new XMLSerializer();
    }
    serializer.setOutputByteStream(stream);
    super.setHandler(serializer.asContentHandler());
    this.objectToMarshall = objectToMarshall;
    this.stream = stream;
    this.nonamespaces = nonamespaces;
    this.includeXmlns = includeXmlns;
  }



  @Override
  public void startDocument() throws SAXException {
    log.info("document started !!!!");
    if (!nonamespaces)
      super.startDocument();
  }

  @Override
  public void endDocument() throws SAXException {
    log.info("document ended !!!!");
    if (!nonamespaces)
      super.endDocument();
  }

  @Override
  public void startElement(String uri, String localName, String qname, Attributes attributes)
      throws SAXException {
    if (nonamespaces) {
      // set xmlns fot root element
      if (!super.rooIgnored && includeXmlns) {
        AttributesImpl imp = new AttributesImpl(attributes);
        if (!StringUtil.isNullOrEmpty(uri)) {
          imp.addAttribute("", "xmlns", "", "", uri);
          attributes = imp;
        }
      }
      uri = "";
      qname = localName;
    }
    super.startElement(uri, localName, qname, attributes);
  }

  @Override
  public void endElement(String uri, String localName, String qname) throws SAXException {
    if (nonamespaces) {
      uri = "";
      qname = localName;
    }
    super.endElement(uri, localName, qname);
  }



  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    log.debug("prefixes " + prefix + "  " + uri);
    if (prefix == "ns2") {
      super.startPrefixMapping("", uri);
    }
    if (!nonamespaces)
      super.startPrefixMapping(prefix, uri);
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    if (!nonamespaces)
      super.endPrefixMapping(prefix);
  }

  @Override
  @Loggable(Loggable.TRACE)
  protected void handleBigDataStartElement(Field dataField, Stack<String> currentPath,
      Stack<String> currentObjPath, String uri, String localName, String qname,
      Attributes attributes) throws DhxException {
    log.debug("Start element of big data field marshalling");
    FileReader fr = null;
    BufferedReader br = null;
    try {
      File file = (File) ReflectionUtil.getObjectByPath(currentObjPath, objectToMarshall);
      fr = new FileReader(file);
      br = new BufferedReader(fr);
      CharBuffer cbf = CharBuffer.allocate(FileUtil.BINARY_BUFFER_SIZE);

      getHandler().startElement(uri, localName, qname, attributes);
      int len;
      while ((len = br.read(cbf)) > 0) {
        handler.characters(cbf.array(), 0, len);
        cbf.flip();
      }
      br.close();
      fr.close();
    } catch (IOException | SAXException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while processing start of the element. "
              + ex.getMessage(),
          ex);
    } finally {
      FileUtil.safeCloseReader(br);
      FileUtil.safeCloseReader(fr);
    }
  }

  @Override
  @Loggable
  protected void handleBigDataCharacters(char[] chars, int start, int length) throws IOException {
    log.error("CHARECTER CANNOT BE HERE!!!!");
  }

  @Override
  @Loggable(Loggable.TRACE)
  protected void handleBigDataEndElement(String uri, String localName, String qname)
      throws IOException, SAXException {
    getHandler().endElement(uri, localName, qname);
  }

}

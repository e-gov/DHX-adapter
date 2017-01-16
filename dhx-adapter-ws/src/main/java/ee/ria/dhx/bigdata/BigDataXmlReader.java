package ee.ria.dhx.bigdata;

import com.jcabi.aspects.Loggable;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper of {@link XMLReader} made for big data files. Contenthandler is always wrapped in
 * {@link BigDataHandler} to handle big data files.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class BigDataXmlReader implements XMLReader {

  @Getter
  private List<BigDataElement> bigDataElements = new ArrayList<BigDataElement>();

  @Getter
  private XMLReader xmlReader;

  private Class<? extends Object> bigDataClass;

  public BigDataXmlReader(XMLReader xmlReader, Class<? extends Object> bigDataClass) {
    this.xmlReader = xmlReader;
    this.bigDataClass = bigDataClass;
  }

  @Override
  public boolean getFeature(String name) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    return getXmlReader().getFeature(name);
  }

  @Override
  public void setFeature(String name, boolean value) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    getXmlReader().setFeature(name, value);

  }

  @Override
  public Object getProperty(String name) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    return getXmlReader().getProperty(name);
  }

  @Override
  public void setProperty(String name, Object value) throws SAXNotRecognizedException,
      SAXNotSupportedException {
    getXmlReader().setProperty(name, value);

  }

  @Override
  public void setEntityResolver(EntityResolver resolver) {
    getXmlReader().setEntityResolver(resolver);

  }

  @Override
  public EntityResolver getEntityResolver() {
    return getXmlReader().getEntityResolver();
  }

  @Override
  public void setDTDHandler(DTDHandler handler) {
    getXmlReader().setDTDHandler(handler);

  }

  @Override
  public DTDHandler getDTDHandler() {
    return getXmlReader().getDTDHandler();
  }

  @Override
  @Loggable
  public void setContentHandler(ContentHandler handler) {
    getXmlReader()
        .setContentHandler(new BigDataUnmarshallHandler(handler, bigDataClass, bigDataElements));

  }

  @Override
  @Loggable
  public ContentHandler getContentHandler() {
    if (getXmlReader().getContentHandler() == null) {
      log.debug("handler is null setting default");
      this.setContentHandler(new DefaultHandler());
    }
    return getXmlReader().getContentHandler();
  }

  @Override
  public void setErrorHandler(ErrorHandler handler) {
    getXmlReader().setErrorHandler(handler);

  }

  @Override
  public ErrorHandler getErrorHandler() {
    return getXmlReader().getErrorHandler();
  }

  @Override
  public void parse(InputSource input) throws IOException, SAXException {
    getXmlReader().parse(input);

  }

  @Override
  public void parse(String systemId) throws IOException, SAXException {
    getXmlReader().parse(systemId);

  }

}

package ee.ria.dhx.bigdata;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.bigdata.annotation.BigDataXmlElement;
import ee.ria.dhx.exception.DhxException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;


/**
 * Wrapper handler which finds {@link BigDataXmlElement} annotated elements in XML and writes them
 * to file in order not to read the whole big data into memory. Other elements are delegetad to the
 * handler which is being wrapped(private member of this class). Big data elements are expected to
 * be of {@link File} type.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public abstract class BigDataHandler implements ContentHandler {

  @Getter
  @Setter
  ContentHandler handler;

  private boolean isDocument = false;

  @Getter
  @Setter
  private Class<? extends Object> bigDataClass = null;
  protected boolean rooIgnored = false;
  private boolean isDesiredClass = true;


  /**
   * Represents current path in XML.
   */
  private Stack<String> currentPath = new Stack<String>();

  private Stack<String> currentObjPath = new Stack<String>();

  private String curList;
  private Integer curIndex = 0;

  protected abstract void handleBigDataStartElement(Field dataField, Stack<String> currentPath,
      Stack<String> currentObjPath, String uri, String localName, String qname,
      Attributes attributes) throws DhxException;

  protected abstract void handleBigDataCharacters(char[] chars) throws IOException;

  protected abstract void handleBigDataEndElement(String uri, String localName, String qname)
      throws IOException, SAXException;

  /**
   * BigDataHandler contructor.
   * 
   * @param handler content handler which will process all not BIG DATA elements
   * @param bigDataClass class which is planned to parse BIG DATA in or null
   */
  public BigDataHandler(ContentHandler handler, Class<? extends Object> bigDataClass) {
    this.handler = handler;
    this.bigDataClass = bigDataClass;
  }

  /**
   * BigDataHandler  contructor.
   * 
   * @param bigDataClass class which is planned to parse BIG DATA in or null
   */
  public BigDataHandler(Class<? extends Object> bigDataClass) {
    this.bigDataClass = bigDataClass;
  }
  
  /**
   * BigDataHandler  contructor.
   * 
   * @param bigDataClass class which is planned to parse BIG DATA in or null
   * @param nonamespaces is marshaller should remove namespaces
   */
  public BigDataHandler(Class<? extends Object> bigDataClass, Boolean nonamespaces) {
    this.bigDataClass = bigDataClass;
  }

  @Override
  // @Loggable
  public void startElement(String uri, String localName, String qname, Attributes attributes)
      throws SAXException {
    try {
      Field dataField = null;
      log.trace("CurList {} curIndex {}", (curList == null ? "" : curList), curIndex);
      // ignore root element in path
      if (!rooIgnored) {
        rooIgnored = true;
        isDesiredClass = ReflectionUtil.isXmlRoot(bigDataClass, localName);
        // if we know which class we are planning to parse big data in and we are parsing that class
      } else if (bigDataClass != null && isDesiredClass) {
        // create path to know where current element is actually located
        currentPath.add(localName);
        // find field in capsule object for corresponding path
        dataField = ReflectionUtil.getFieldForPath(currentPath, bigDataClass);
        // if current element is list, then need to put that into path also
        if (dataField != null) {
          if (List.class.isAssignableFrom(dataField.getType())) {
            log.debug("Found list type field {}", dataField.getName());
            if (curList != null && !curList.equals(dataField.getName())) {
              curIndex = 0;
            }
            curList = dataField.getName();
            currentObjPath.add(dataField.getName() + "[" + curIndex + "]");
            curIndex++;
          } else {
            curList = null;
            curIndex = 0;
            currentObjPath.add(dataField.getName());
          }
        }
      }
      if (dataField != null && ReflectionUtil.isBigDataField(dataField)) {
        log.debug("Start element of big data field");
        isDocument = true;
        BigDataXmlElement annotation = dataField.getAnnotation(BigDataXmlElement.class);
        if (annotation != null) {
          String name = annotation.name().intern();
          qname = qname.replace(localName, name);
          localName = name;
        }
        handleBigDataStartElement(dataField, currentPath, currentObjPath, uri, localName, qname,
            attributes);
      } else {
        log.trace("handler " + handler.getClass().getCanonicalName());
        handler.startElement(uri, localName, qname, attributes);
      }
    } catch (DhxException ex) {
      throw new SAXException(ex);
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    if (isDocument) {
      try {
        char[] charSub;
        if (start > 0 || ch.length > length) {
          charSub = Arrays.copyOfRange(ch, start, start + length);
        } else {
          charSub = ch;
        }
        handleBigDataCharacters(charSub);
      } catch (IOException ex) {
        throw new SAXException("Error occured while reading document." + ex.getMessage(), ex);
      }
    } else {
      handler.characters(ch, start, length);
    }
  }

  @Override
  @Loggable(Loggable.TRACE)
  public void endElement(String uri, String localName, String qname) throws SAXException {
    if (currentObjPath.size() > 0) {
      String element = currentObjPath.pop();
      // if closing element is of List type, then need to put current list and current index,
      // because next element might be next member of the same list
      if (element.endsWith("]")) {
        Integer indexOfArray = element.indexOf("[");
        String list = element.substring(0, indexOfArray);
        Integer index =
            Integer.parseInt(element.substring(indexOfArray + 1, element.length() - 1));
        curList = list;
        curIndex = index + 1;
      }
    }
    if (currentPath.size() > 0) {
      currentPath.pop();
    }
    if (isDocument) {
      try {
        isDocument = false;
        handleBigDataEndElement(uri, localName, qname);
      } catch (IOException ex) {
        throw new SAXException("Error occured while processing wnd of the element. "
            + ex.getMessage(), ex);
      }
    } else {
      handler.endElement(uri, localName, qname);
    }
  }

  @Override
  public void endDocument() throws SAXException {
    getHandler().endDocument();
  }

  @Override
  public void endPrefixMapping(String prefix) throws SAXException {
    getHandler().endPrefixMapping(prefix);

  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
    getHandler().ignorableWhitespace(ch, start, length);

  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    getHandler().processingInstruction(target, data);

  }

  @Override
  public void setDocumentLocator(Locator locator) {
    getHandler().setDocumentLocator(locator);

  }

  @Override
  public void skippedEntity(String name) throws SAXException {
    getHandler().skippedEntity(name);

  }

  @Override
  public void startDocument() throws SAXException {
    getHandler().startDocument();

  }

  @Override
  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    getHandler().startPrefixMapping(prefix, uri);

  }

}

package ee.ria.dhx.bigdata;


import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Stack;

/**
 * BIG DATA content handler specific for object unmarshalling.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
public class BigDataUnmarshallHandler extends BigDataHandler {

  private Writer writer;


  private List<BigDataElement> bigDataElements;


  /**
   * 
   * @param handler - content handler which will process all not BIG DATA elements.
   * @param bigDataClass class having big data that is being unmarshalled or null if no big datat is
   *        expected
   * @param bigDataElements - list of big data elements to which found big data elements will be
   *        written
   */
  public BigDataUnmarshallHandler(ContentHandler handler, Class<? extends Object> bigDataClass,
      List<BigDataElement> bigDataElements) {
    super(handler, bigDataClass);
    this.bigDataElements = bigDataElements;
  }

  @SuppressWarnings("unchecked")
  @Override
  @Loggable(Loggable.TRACE)
  protected void handleBigDataStartElement(Field dataField, Stack<String> currentPath,
      Stack<String> currentObjPath, String uri, String localName, String qname,
      Attributes attributes) throws DhxException {
    log.debug("Start element of big data field");
    try {
      File file = FileUtil.createPipelineFile();
      writer = new BufferedWriter(new FileWriter(file));
      BigDataElement element = new BigDataElement();
      element.setFile(file);
      element.setField(dataField);
      element.setXmlPath((Stack<String>) currentPath.clone());
      element.setObjPath((Stack<String>) currentObjPath.clone());
      bigDataElements.add(element);
      getHandler().startElement(uri, localName, qname, attributes);
    } catch (IOException | SAXException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while processing start of the element. "
              + ex.getMessage(),
          ex);
    }
  }

  @Override
  @Loggable(Loggable.TRACE)
  protected void handleBigDataCharacters(char[] chars, int start, int length) throws IOException {
    writer.write(chars, start, length);
  }

  @Override
  @Loggable(Loggable.TRACE)
  protected void handleBigDataEndElement(String uri, String localName, String qname)
      throws IOException, SAXException {
    writer.close();
    getHandler().endElement(uri, localName, qname);
  }

}

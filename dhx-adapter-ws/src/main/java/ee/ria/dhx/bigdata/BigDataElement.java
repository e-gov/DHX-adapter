package ee.ria.dhx.bigdata;

import lombok.Setter;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Stack;

/**
 * Class representing big data element in XML.
 * 
 * @author Aleksei Kokarev
 *
 */
@Setter
public class BigDataElement {

  private Field field;
  private File file;
  private Stack<String> xmlPath;
  private Stack<String> objPath;

  /**
   * Class's field which represents current big data element.
   * 
   * @return the field
   */
  public Field getField() {
    return field;
  }

  /**
   * File which contains data of current big data element.
   * 
   * @return the file
   */
  public File getFile() {
    return file;
  }

  /**
   * Stack of Strings representing path in XML for current big data element.
   * 
   * @return the xmlPath
   */
  public Stack<String> getXmlPath() {
    return xmlPath;
  }

  /**
   * Stack of Strings representing path in Object for current big data element.
   * 
   * @return the objPath
   */
  public Stack<String> getObjPath() {
    return objPath;
  }



}

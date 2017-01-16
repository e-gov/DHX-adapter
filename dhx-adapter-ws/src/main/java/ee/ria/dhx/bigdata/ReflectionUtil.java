package ee.ria.dhx.bigdata;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.bigdata.annotation.BigDataXmlElement;
import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Stack;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Utility methods related to reflection.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class ReflectionUtil {

  private static final String TYPE_NAME_PREFIX = "class ";

  private static String getClassName(Type type) {
    if (type == null) {
      return "";
    }
    String className = type.toString();
    if (className.startsWith(TYPE_NAME_PREFIX)) {
      className = className.substring(TYPE_NAME_PREFIX.length());
    }
    return className;
  }

  private static Class<?> getClass(Type type)
      throws DhxException {
    try {
      String className = getClassName(type);
      if (className == null || className.isEmpty()) {
        return null;
      }
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Unable to find class for type. "
          + ex.getMessage(), ex);
    }
  }

  /**
   * Method finds field in class according to it's path.
   * 
   * @param path - {@link Stack} of Strings representing path in object
   * @param fieldClass - class in which we are searching for the field
   * @return - field found in class
   * @throws DhxException - thrown if error occurs
   */
  @Loggable
  public static Field getFieldForPath(Stack<String> path, Class<? extends Object> fieldClass)
      throws DhxException {
    Class<? extends Object> curClass = fieldClass;
    Field curField = null;
    for (String member : path) {
      log.trace("seraching for path member {} in class {}", member, curClass.getName());
      curField = null;
      for (Field field : curClass.getDeclaredFields()) {
        log.trace("scanning field {}", field.getName());
        if (field.isAnnotationPresent(XmlElement.class)) {
          XmlElement annotation = (XmlElement) field.getAnnotation(XmlElement.class);
          log.trace("annotation name: {}  qName: {}", annotation.name(), member);
          if (annotation.name().equalsIgnoreCase(member)) {
            if (List.class.isAssignableFrom(field.getType())) {
              log.trace("Found List type of field");
              ParameterizedType listType = (ParameterizedType) field.getGenericType();
              // expect only variables with single generic parameter
              curClass = getClass(listType.getActualTypeArguments()[0]);
            } else {
              curClass = field.getType();
            }
            curField = field;
            break;
          }
        } else if (field.isAnnotationPresent(BigDataXmlElement.class)) {
          BigDataXmlElement annotation =
              (BigDataXmlElement) field.getAnnotation(BigDataXmlElement.class);
          if (annotation.name().equalsIgnoreCase(member)) {
            curField = field;
            curClass = field.getType();
            break;
          }
        }
      }
      /*
       * if (curField == null) { throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
       * "Unable to find field in object accoring to path. {}" + path); }
       */
    }
    return curField;
  }

  /**
   * Method to define if class's field is big data field(defined by annotation
   * {@link BigDataXmlElement}).
   * 
   * @param field - field to check if it is big data field
   * @return - true if given field is big data field
   * @throws DhxException - thrown when error occurs
   */
  @Loggable
  public static Boolean isBigDataField(Field field)
      throws DhxException {
    if (field != null && field.isAnnotationPresent(BigDataXmlElement.class)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Method searches if given class has {@link XmlRootElement} and if XmlRootElement's name is the
   * same as rootElement in input.
   * 
   * @param classToCheck class to find XmlRootElement in
   * @param rootElement root element's name
   * @return
   */
  @Loggable
  public static boolean isXmlRoot(Class<? extends Object> classToCheck, String rootElement) {
    XmlRootElement rootAnnotation =
        (XmlRootElement) classToCheck.getAnnotation(XmlRootElement.class);
    if (rootAnnotation != null && rootAnnotation.name().equalsIgnoreCase(rootElement)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Method sets big data fields to given object. Big data elements are set according to paths that
   * are defined in them.
   * 
   * @param bigDataElements - list of big data elements to set
   * @param obj - object to which big data elements are set
   * @throws DhxException - throws if error occurs
   */
  @Loggable
  public static void setBigDataFieldsToObject(List<BigDataElement> bigDataElements, Object obj)
      throws DhxException {
    for (BigDataElement element : bigDataElements) {
      log.debug("setting bigelement.");
      Stack<String> path = element.getObjPath();
      setOrGetObjectByPath(path, obj, element.getFile());
    }
  }

  /**
   * Method finds property in object by path and returns it.
   * 
   * @param path - {@link Stack} of Strings representing path in object
   * @param obj - object in which we are getting property
   * @return - object return by get method
   * @throws DhxException - thrown if error occurs
   */
  @Loggable
  public static Object getObjectByPath(Stack<String> path, Object obj) throws DhxException {
    return setOrGetObjectByPath(path, obj, null);
  }

  /**
   * Method finds property in object by path and gets it if objectToset is null and sets objectToset
   * to property if it is not null.
   * 
   * @param path - {@link Stack} of Strings representing path in object
   * @param obj - object in which we are getting or settting property
   * @param objectToset - object which needs to be set. if null, then get method is invoked
   * @return - object which were set or returned by get method
   * @throws DhxException - thrown if error occurs
   */
  @Loggable
  protected static Object setOrGetObjectByPath(Stack<String> path, Object obj, Object objectToset)
      throws DhxException {
    try {
      log.debug("setting bigelement.");
      Class<? extends Object> curClass = obj.getClass();
      Object curObj = obj;
      for (String member : path) {
        log.trace("scanning path  {} for class {}", member, curObj.getClass().getName());
        String curMember = member;
        Integer curIndex = 0;
        Boolean isList = false;
        if (member.endsWith("]")) {
          Integer indexOfArray = member.indexOf("[");
          String list = member.substring(0, indexOfArray);
          Integer index =
              Integer.parseInt(member.substring(indexOfArray + 1, member.length() - 1));
          curMember = list;
          curIndex = index;
          isList = true;
        }
        Boolean found = false;
        for (Field field : curClass.getDeclaredFields()) {
          log.trace("scanning path {} and field {}", curMember, field.getName());
          if (field.getName().equals(curMember)) {
            if (isBigDataField(field)) {
              if (objectToset != null) {
                Method method =
                    curObj.getClass().getDeclaredMethod(
                        getFieldSetter(field.getName()),
                        new Class[] {objectToset.getClass()});
                method.invoke(curObj, objectToset);
                curObj = objectToset;
              } else {
                Method method =
                    curObj.getClass().getDeclaredMethod(
                        getFieldGetter(field.getName()),
                        new Class[] {});
                curObj = method.invoke(curObj);
              }
              found = true;
              break;
            } else {
              Method method =
                  curObj.getClass().getDeclaredMethod(
                      getFieldGetter(field.getName()),
                      new Class[] {});
              log.trace("executing method {}" + getFieldGetter(field.getName()));
              curObj = method.invoke(curObj);
              if (isList) {
                log.trace("executing method get {}" + curIndex);
                // here we expect it to be List
                curObj = ((List<?>) curObj).get(curIndex);
              }
              curClass = curObj.getClass();
              found = true;
              break;
            }
          }
        }
        if (!found) {
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
              "Unable to find field in object accoring to path. " + path);
        }
      }
      return curObj;
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
      log.error(ex.getMessage(), ex);
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Unable to set BIG DATA elements to object. " + ex.getMessage(), ex);
    }
  }

  @Loggable
  private static String getFieldGetter(String fieldName) {
    return "get" + StringUtil.capitalize(fieldName);
  }

  @Loggable
  private static String getFieldSetter(String fieldName) {
    return "set" + StringUtil.capitalize(fieldName);
  }

}

package ee.ria.dhx.util;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Utility methods for convertation.
 * 
 * @author Aleksei Kokarev
 *
 */
public class ConversionUtil {

  /**
   * Converts Date to XML date.
   * 
   * @param date - date to convert
   * @return - converted date
   * @throws DhxException - thrown if error occurs
   */
  public static XMLGregorianCalendar toGregorianCalendar(Date date) throws DhxException {
    if (date == null) {
      return null;
    }
    try {
      GregorianCalendar gcalendar = new GregorianCalendar();
      gcalendar.setTime(date);
      XMLGregorianCalendar xmlDate =
          DatatypeFactory.newInstance().newXMLGregorianCalendar(gcalendar);
      return xmlDate;
    } catch (DatatypeConfigurationException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while converting date. " + ex.getMessage(), ex);
    }
  }

  /**
   * Converts XML date to Date.
   * 
   * @param xmlDate - XML date to convert
   * @return - converted Date
   */
  public static Date toDate(XMLGregorianCalendar xmlDate) {
    if (xmlDate == null) {
      return null;
    }
    return xmlDate.toGregorianCalendar().getTime();

  }

}

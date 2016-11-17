package ee.ria.dhx.util;

public class StringUtil {


  /**
   * Method controlls if string is null or empty string and return true if it is, otherwise return
   * false.
   * 
   * @param str - string to test
   * @return - true if string is null or empty string
   */
  public static boolean isNullOrEmpty(String str) {
    if (str == null || str.equals("")) {
      return true;
    } else {
      return false;
    }
  }


  public static String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

}

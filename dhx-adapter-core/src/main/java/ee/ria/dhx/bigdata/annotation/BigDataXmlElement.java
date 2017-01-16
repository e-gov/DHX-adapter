package ee.ria.dhx.bigdata.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
// can use in method only.
public @interface BigDataXmlElement {

  /**
   * Returns name of the corresponding XML element.
   * @return - name of the corresponding XML element
   */
  public String name();

}

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.17 at 11:49:24 AM EET
//


package ee.ria.dhx.server.types.ee.riik.schemas.dhl.rkel_letter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>
 * Java class for IntellectualPropertyRightsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IntellectualPropertyRightsType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="CopyrightEndDate" type="{http://www.w3.org/2001/XMLSchema}date"/&gt;
 *         &lt;element name="IprOwner" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IntellectualPropertyRightsType", propOrder = {
    "copyrightEndDate",
    "iprOwner"
})
public class IntellectualPropertyRightsType {

  @XmlElement(name = "CopyrightEndDate", required = true)
  @XmlSchemaType(name = "date")
  protected XMLGregorianCalendar copyrightEndDate;
  @XmlElement(name = "IprOwner", required = true)
  protected String iprOwner;

  /**
   * Gets the value of the copyrightEndDate property.
   * 
   * @return possible object is {@link XMLGregorianCalendar }
   * 
   */
  public XMLGregorianCalendar getCopyrightEndDate() {
    return copyrightEndDate;
  }

  /**
   * Sets the value of the copyrightEndDate property.
   * 
   * @param value allowed object is {@link XMLGregorianCalendar }
   * 
   */
  public void setCopyrightEndDate(XMLGregorianCalendar value) {
    this.copyrightEndDate = value;
  }

  /**
   * Gets the value of the iprOwner property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getIprOwner() {
    return iprOwner;
  }

  /**
   * Sets the value of the iprOwner property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setIprOwner(String value) {
    this.iprOwner = value;
  }

}

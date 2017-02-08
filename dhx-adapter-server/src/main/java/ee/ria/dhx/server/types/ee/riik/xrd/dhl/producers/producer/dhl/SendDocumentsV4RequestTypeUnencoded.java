//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference
// Implementation, v2.2.11
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2016.11.17 at 11:49:24 AM EET
//


package ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl;

import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>
 * Java class for sendDocumentsV4RequestTypeUnencoded complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="sendDocumentsV4RequestTypeUnencoded"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dokumendid"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.riik.ee/schemas/deccontainer/vers_2_1/}DecContainer"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="kaust" type="{http://www.riik.ee/schemas/dhl-meta-automatic}dhlDokTaisnimiType" minOccurs="0"/&gt;
 *         &lt;element name="sailitustahtaeg" type="{http://www.w3.org/2001/XMLSchema}date" minOccurs="0"/&gt;
 *         &lt;element name="edastus_id" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="fragment_nr" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="fragmente_kokku" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendDocumentsV4RequestTypeUnencoded", propOrder = {
    "dokumendid",
    "kaust",
    "sailitustahtaeg",
    "edastusId",
    "fragmentNr",
    "fragmenteKokku"
})
public class SendDocumentsV4RequestTypeUnencoded {

  @XmlElement(required = true)
  protected SendDocumentsV4RequestTypeUnencoded.Dokumendid dokumendid;
  protected String kaust;
  @XmlSchemaType(name = "date")
  protected XMLGregorianCalendar sailitustahtaeg;
  @XmlElement(name = "edastus_id")
  protected String edastusId;
  @XmlElement(name = "fragment_nr")
  protected BigInteger fragmentNr;
  @XmlElement(name = "fragmente_kokku")
  protected BigInteger fragmenteKokku;

  /**
   * Gets the value of the dokumendid property.
   * 
   * @return possible object is {@link SendDocumentsV4RequestTypeUnencoded.Dokumendid }
   * 
   */
  public SendDocumentsV4RequestTypeUnencoded.Dokumendid getDokumendid() {
    return dokumendid;
  }

  /**
   * Sets the value of the dokumendid property.
   * 
   * @param value allowed object is {@link SendDocumentsV4RequestTypeUnencoded.Dokumendid }
   * 
   */
  public void setDokumendid(SendDocumentsV4RequestTypeUnencoded.Dokumendid value) {
    this.dokumendid = value;
  }

  /**
   * Gets the value of the kaust property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getKaust() {
    return kaust;
  }

  /**
   * Sets the value of the kaust property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setKaust(String value) {
    this.kaust = value;
  }

  /**
   * Gets the value of the sailitustahtaeg property.
   * 
   * @return possible object is {@link XMLGregorianCalendar }
   * 
   */
  public XMLGregorianCalendar getSailitustahtaeg() {
    return sailitustahtaeg;
  }

  /**
   * Sets the value of the sailitustahtaeg property.
   * 
   * @param value allowed object is {@link XMLGregorianCalendar }
   * 
   */
  public void setSailitustahtaeg(XMLGregorianCalendar value) {
    this.sailitustahtaeg = value;
  }

  /**
   * Gets the value of the edastusId property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getEdastusId() {
    return edastusId;
  }

  /**
   * Sets the value of the edastusId property.
   * 
   * @param value allowed object is {@link String }
   * 
   */
  public void setEdastusId(String value) {
    this.edastusId = value;
  }

  /**
   * Gets the value of the fragmentNr property.
   * 
   * @return possible object is {@link BigInteger }
   * 
   */
  public BigInteger getFragmentNr() {
    return fragmentNr;
  }

  /**
   * Sets the value of the fragmentNr property.
   * 
   * @param value allowed object is {@link BigInteger }
   * 
   */
  public void setFragmentNr(BigInteger value) {
    this.fragmentNr = value;
  }

  /**
   * Gets the value of the fragmenteKokku property.
   * 
   * @return possible object is {@link BigInteger }
   * 
   */
  public BigInteger getFragmenteKokku() {
    return fragmenteKokku;
  }

  /**
   * Sets the value of the fragmenteKokku property.
   * 
   * @param value allowed object is {@link BigInteger }
   * 
   */
  public void setFragmenteKokku(BigInteger value) {
    this.fragmenteKokku = value;
  }


  /**
   * <p>
   * Java class for anonymous complex type.
   * 
   * <p>
   * The following schema fragment specifies the expected content contained within this class.
   * 
   * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.riik.ee/schemas/deccontainer/vers_2_1/}DecContainer"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
   * 
   * 
   */
  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(name = "", propOrder = {
      "decContainer"
  })
  public static class Dokumendid {

    @XmlElement(name = "DecContainer", namespace = "http://www.riik.ee/schemas/deccontainer/vers_2_1/", required = true)
    protected DecContainer decContainer;

    /**
     * Gets the value of the decContainer property.
     * 
     * @return possible object is {@link DecContainer }
     * 
     */
    public DecContainer getDecContainer() {
      return decContainer;
    }

    /**
     * Sets the value of the decContainer property.
     * 
     * @param value allowed object is {@link DecContainer }
     * 
     */
    public void setDecContainer(DecContainer value) {
      this.decContainer = value;
    }

  }

}
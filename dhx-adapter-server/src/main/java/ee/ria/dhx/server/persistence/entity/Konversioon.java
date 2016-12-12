package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;


/**
 * The persistent class for the konversioon database table.
 * 
 */
@Entity
@NamedQuery(name = "Konversioon.findAll", query = "SELECT k FROM Konversioon k")
public class Konversioon implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  private Integer id;

  @Column(name = "result_version")
  private Integer resultVersion;

  private Integer version;

  private String xslt;

  public Konversioon() {}

  public Integer getId() {
    return this.id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getResultVersion() {
    return this.resultVersion;
  }

  public void setResultVersion(Integer resultVersion) {
    this.resultVersion = resultVersion;
  }

  public Integer getVersion() {
    return this.version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public String getXslt() {
    return this.xslt;
  }

  public void setXslt(String xslt) {
    this.xslt = xslt;
  }

}

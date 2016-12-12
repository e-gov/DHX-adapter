package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;


/**
 * The persistent class for the allkiri database table.
 * 
 */
@Entity
@NamedQuery(name = "Allkiri.findAll", query = "SELECT a FROM Allkiri a")
public class Allkiri implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "allkiri_id")
  private Integer allkiriId;

  private String eesnimi;

  private String indeks;

  private String isikukood;

  private Timestamp kuupaev;

  private String linn;

  private String maakond;

  private String perenimi;

  private String riik;

  private String roll;

  // bi-directional many-to-one association to Dokument
  @ManyToOne
  @JoinColumn(name = "dokument_id")
  private Document dokument;

  public Allkiri() {}

  public Integer getAllkiriId() {
    return this.allkiriId;
  }

  public void setAllkiriId(Integer allkiriId) {
    this.allkiriId = allkiriId;
  }

  public String getEesnimi() {
    return this.eesnimi;
  }

  public void setEesnimi(String eesnimi) {
    this.eesnimi = eesnimi;
  }

  public String getIndeks() {
    return this.indeks;
  }

  public void setIndeks(String indeks) {
    this.indeks = indeks;
  }

  public String getIsikukood() {
    return this.isikukood;
  }

  public void setIsikukood(String isikukood) {
    this.isikukood = isikukood;
  }

  public Timestamp getKuupaev() {
    return this.kuupaev;
  }

  public void setKuupaev(Timestamp kuupaev) {
    this.kuupaev = kuupaev;
  }

  public String getLinn() {
    return this.linn;
  }

  public void setLinn(String linn) {
    this.linn = linn;
  }

  public String getMaakond() {
    return this.maakond;
  }

  public void setMaakond(String maakond) {
    this.maakond = maakond;
  }

  public String getPerenimi() {
    return this.perenimi;
  }

  public void setPerenimi(String perenimi) {
    this.perenimi = perenimi;
  }

  public String getRiik() {
    return this.riik;
  }

  public void setRiik(String riik) {
    this.riik = riik;
  }

  public String getRoll() {
    return this.roll;
  }

  public void setRoll(String roll) {
    this.roll = roll;
  }

  public Document getDokument() {
    return this.dokument;
  }

  public void setDokument(Document dokument) {
    this.dokument = dokument;
  }

}

package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;


/**
 * The persistent class for the logi database table.
 * 
 */
@Entity
@NamedQuery(name = "Logi.findAll", query = "SELECT l FROM Logi l")
public class Logi implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "log_id")
  private Integer logId;

  @Column(name = "ab_kasutaja")
  private String abKasutaja;

  private Integer ametikoht;

  private String comm;

  private Timestamp created;

  private String ctype;

  @Column(name = "ef_kasutaja")
  private String efKasutaja;

  @Column(name = "kasutaja_kood")
  private String kasutajaKood;

  @Column(name = "last_modified")
  private Timestamp lastModified;

  @Column(name = "muutmise_aeg")
  private Timestamp muutmiseAeg;

  private String op;

  private String tabel;

  @Column(name = "tabel_uid")
  private Integer tabelUid;

  private String uidcol;

  private String username;

  @Column(name = "uus_vaartus")
  private String uusVaartus;

  @Column(name = "vana_vaartus")
  private String vanaVaartus;

  private String veerg;

  @Column(name = "xtee_asutus")
  private String xteeAsutus;

  @Column(name = "xtee_isikukood")
  private String xteeIsikukood;

  public Logi() {}

  public Integer getLogId() {
    return this.logId;
  }

  public void setLogId(Integer logId) {
    this.logId = logId;
  }

  public String getAbKasutaja() {
    return this.abKasutaja;
  }

  public void setAbKasutaja(String abKasutaja) {
    this.abKasutaja = abKasutaja;
  }

  public Integer getAmetikoht() {
    return this.ametikoht;
  }

  public void setAmetikoht(Integer ametikoht) {
    this.ametikoht = ametikoht;
  }

  public String getComm() {
    return this.comm;
  }

  public void setComm(String comm) {
    this.comm = comm;
  }

  public Timestamp getCreated() {
    return this.created;
  }

  public void setCreated(Timestamp created) {
    this.created = created;
  }

  public String getCtype() {
    return this.ctype;
  }

  public void setCtype(String ctype) {
    this.ctype = ctype;
  }

  public String getEfKasutaja() {
    return this.efKasutaja;
  }

  public void setEfKasutaja(String efKasutaja) {
    this.efKasutaja = efKasutaja;
  }

  public String getKasutajaKood() {
    return this.kasutajaKood;
  }

  public void setKasutajaKood(String kasutajaKood) {
    this.kasutajaKood = kasutajaKood;
  }

  public Timestamp getLastModified() {
    return this.lastModified;
  }

  public void setLastModified(Timestamp lastModified) {
    this.lastModified = lastModified;
  }

  public Timestamp getMuutmiseAeg() {
    return this.muutmiseAeg;
  }

  public void setMuutmiseAeg(Timestamp muutmiseAeg) {
    this.muutmiseAeg = muutmiseAeg;
  }

  public String getOp() {
    return this.op;
  }

  public void setOp(String op) {
    this.op = op;
  }

  public String getTabel() {
    return this.tabel;
  }

  public void setTabel(String tabel) {
    this.tabel = tabel;
  }

  public Integer getTabelUid() {
    return this.tabelUid;
  }

  public void setTabelUid(Integer tabelUid) {
    this.tabelUid = tabelUid;
  }

  public String getUidcol() {
    return this.uidcol;
  }

  public void setUidcol(String uidcol) {
    this.uidcol = uidcol;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getUusVaartus() {
    return this.uusVaartus;
  }

  public void setUusVaartus(String uusVaartus) {
    this.uusVaartus = uusVaartus;
  }

  public String getVanaVaartus() {
    return this.vanaVaartus;
  }

  public void setVanaVaartus(String vanaVaartus) {
    this.vanaVaartus = vanaVaartus;
  }

  public String getVeerg() {
    return this.veerg;
  }

  public void setVeerg(String veerg) {
    this.veerg = veerg;
  }

  public String getXteeAsutus() {
    return this.xteeAsutus;
  }

  public void setXteeAsutus(String xteeAsutus) {
    this.xteeAsutus = xteeAsutus;
  }

  public String getXteeIsikukood() {
    return this.xteeIsikukood;
  }

  public void setXteeIsikukood(String xteeIsikukood) {
    this.xteeIsikukood = xteeIsikukood;
  }

}

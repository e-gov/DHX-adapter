package ee.ria.dhx.server.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * The persistent class for the dokumendi_ajalugu database table.
 * 
 */
@Entity
@Table(name = "dokumendi_ajalugu")
@NamedQuery(name = "DokumendiAjalugu.findAll", query = "SELECT d FROM DokumendiAjalugu d")
public class DokumendiAjalugu implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "ajalugu_id")
  private Integer ajaluguId;

  private String metainfo;

  private String metaxml;

  private String transport;

  // bi-directional many-to-one association to Dokument
  @ManyToOne
  @JoinColumn(name = "dokument_id")
  private Document dokument;

  public DokumendiAjalugu() {}

  public Integer getAjaluguId() {
    return this.ajaluguId;
  }

  public void setAjaluguId(Integer ajaluguId) {
    this.ajaluguId = ajaluguId;
  }

  public String getMetainfo() {
    return this.metainfo;
  }

  public void setMetainfo(String metainfo) {
    this.metainfo = metainfo;
  }

  public String getMetaxml() {
    return this.metaxml;
  }

  public void setMetaxml(String metaxml) {
    this.metaxml = metaxml;
  }

  public String getTransport() {
    return this.transport;
  }

  public void setTransport(String transport) {
    this.transport = transport;
  }

  public Document getDokument() {
    return this.dokument;
  }

  public void setDokument(Document dokument) {
    this.dokument = dokument;
  }

}

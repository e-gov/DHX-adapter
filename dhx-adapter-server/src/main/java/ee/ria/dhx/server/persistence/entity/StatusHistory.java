package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Lob;

/**
 * The persistent class for the staatuse_ajalugu database table.
 * 
 */
@Entity
@Table(name = "staatuse_ajalugu")
public class StatusHistory implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "staatuse_ajalugu_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer statusHistoryId;

  @Column(name = "fault_actor")
  private String faultActor;

  @Column(name = "fault_code")
  private String faultCode;

  @Column(name = "fault_detail", length = 2000)
  private String faultDetail;

  @Column(name = "fault_string")
  private String faultString;

  @Lob
  @Column(name = "meta_xml")
  private String metaxml;

  @Column(name = "staatuse_muutmise_aeg")
  private Timestamp statusChangeDate;

  @Column(name = "vastuvotja_staatus_id")
  private Integer recipientStatusId;

  // bi-directional many-to-one association to Klassifikaator
  // @ManyToOne
  @Column(name = "staatus_id")
  private Integer statusId;

  // bi-directional many-to-one association to Vastuvotja
  @ManyToOne
  @JoinColumn(name = "vastuvotja_id")
  private Recipient recipient;

  public StatusHistory() {}

  /**
   * Returns the statusHistoryId.
   *
   * @return the statusHistoryId
   */
  public Integer getStatusHistoryId() {
    return statusHistoryId;
  }

  /**
   * Sets the statusHistoryId.
   *
   * @param statusHistoryId the statusHistoryId to set
   */
  public void setStatusHistoryId(Integer statusHistoryId) {
    this.statusHistoryId = statusHistoryId;
  }

  /**
   * Returns the faultActor.
   *
   * @return the faultActor
   */
  public String getFaultActor() {
    return faultActor;
  }

  /**
   * Sets the faultActor.
   *
   * @param faultActor the faultActor to set
   */
  public void setFaultActor(String faultActor) {
    this.faultActor = faultActor;
  }

  /**
   * Returns the faultCode.
   *
   * @return the faultCode
   */
  public String getFaultCode() {
    return faultCode;
  }

  /**
   * Sets the faultCode.
   *
   * @param faultCode the faultCode to set
   */
  public void setFaultCode(String faultCode) {
    this.faultCode = faultCode;
  }

  /**
   * Returns the faultDetail.
   *
   * @return the faultDetail
   */
  public String getFaultDetail() {
    return faultDetail;
  }

  /**
   * Sets the faultDetail.
   *
   * @param faultDetail the faultDetail to set
   */
  public void setFaultDetail(String faultDetail) {
    this.faultDetail = faultDetail;
  }

  /**
   * Returns the faultString.
   *
   * @return the faultString
   */
  public String getFaultString() {
    return faultString;
  }

  /**
   * Sets the faultString.
   *
   * @param faultString the faultString to set
   */
  public void setFaultString(String faultString) {
    this.faultString = faultString;
  }

  /**
   * Returns the metaxml.
   *
   * @return the metaxml
   */
  public String getMetaxml() {
    return metaxml;
  }

  /**
   * Sets the metaxml.
   *
   * @param metaxml the metaxml to set
   */
  public void setMetaxml(String metaxml) {
    this.metaxml = metaxml;
  }

  /**
   * Returns the statusChangeDate.
   *
   * @return the statusChangeDate
   */
  public Timestamp getStatusChangeDate() {
    return statusChangeDate;
  }

  /**
   * Sets the statusChangeDate.
   *
   * @param statusChangeDate the statusChangeDate to set
   */
  public void setStatusChangeDate(Timestamp statusChangeDate) {
    this.statusChangeDate = statusChangeDate;
  }

  /**
   * Returns the recipientStatusId.
   *
   * @return the recipientStatusId
   */
  public Integer getRecipientStatusId() {
    return recipientStatusId;
  }

  /**
   * Sets the recipientStatusId.
   *
   * @param recipientStatusId the recipientStatusId to set
   */
  public void setRecipientStatusId(Integer recipientStatusId) {
    this.recipientStatusId = recipientStatusId;
  }

  /**
   * Returns the statusId.
   *
   * @return the statusId
   */
  public Integer getStatusId() {
    return statusId;
  }

  /**
   * Sets the statusId.
   *
   * @param statusId the statusId to set
   */
  public void setStatusId(Integer statusId) {
    this.statusId = statusId;
  }

  /**
   * Returns the recipient.
   *
   * @return the recipient
   */
  public Recipient getRecipient() {
    return recipient;
  }

  /**
   * Sets the recipient.
   *
   * @param recipient the recipient to set
   */
  public void setRecipient(Recipient recipient) {
    this.recipient = recipient;
  }

}

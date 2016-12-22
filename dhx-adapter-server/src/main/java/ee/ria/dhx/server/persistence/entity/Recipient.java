package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

/**
 * The persistent class for the vastuvotja database table.
 * 
 */
@Entity
@Table(name = "vastuvotja")
public class Recipient extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "vastuvotja_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long recipientId;

  @Column(name = "allyksus")
  private String structuralUnit;

  @Column(name = "dhx_external_consignment_id")
  private String dhxExternalConsignmentId;

  @Column(name = "dhx_external_receipt_id")
  private String dhxExternalReceiptId;

  @Column(name = "dhx_internal_consignment_id")
  private String dhxInternalConsignmentId;

  @Column(name = "dok_id_teises_serveris")
  private Integer dokIdTeisesServeris;

  @Column(name = "fault_actor")
  private String faultActor;

  @Column(name = "fault_code")
  private String faultCode;

  @Column(name = "fault_detail", columnDefinition = "TEXT")
  private String faultDetail;

  @Column(name = "fault_string", columnDefinition = "TEXT")
  private String faultString;

  @Column(name = "isikukood")
  private String personalcode;

  @Column(name = "last_send_date")
  private Timestamp lastSendDate;

  @Column(name = "saatmise_algus")
  private Timestamp sendingStart;

  @Column(name = "saatmise_lopp")
  private Timestamp sendingEnd;

  @Column(name = "staatuse_muutmise_aeg")
  private Timestamp statusChangeDate;

  // bi-directional many-to-one association to StaatuseAjalugu
  @OneToMany(mappedBy = "recipient", cascade = {CascadeType.ALL})
  private List<StatusHistory> statusHistory;

  // bi-directional many-to-one association to Asutus
  @ManyToOne
  @JoinColumn(name = "asutus_id")
  private Organisation organisation;

  // bi-directional many-to-one association to Klassifikaator
  // @ManyToOne
  @Column(name = "saatmisviis_id")
  private Integer sendingTypeId;

  // bi-directional many-to-one association to Klassifikaator
  // @ManyToOne
  @Column(name = "staatus_id")
  private Integer statusId;

  // bi-directional many-to-one association to Transport
  @ManyToOne
  @JoinColumn(name = "transport_id")
  private Transport transport;

  @Column(name = "vastuvotja_staatus_id")
  private Integer recipientStatusId;

  private String metaxml;

  public Recipient() {}

  @PrePersist
  public void beforeInsert() {
    insertHistory();
  }


  private void insertHistory() {
    StatusHistory history = new StatusHistory();
    history.setRecipientStatusId(this.getRecipientStatusId());
    history.setRecipient(this);
    history.setStatusChangeDate(this.getStatusChangeDate());
    history.setMetaxml(this.getMetaxml());
    history.setStatusId(this.getStatusId());
    history.setFaultString(this.getFaultString());
    history.setFaultDetail(this.getFaultDetail());
    history.setFaultCode(this.getFaultCode());
    history.setFaultActor(this.getFaultActor());
    this.addStatusHistory(history);
  }

  /**
   * Adds statusHistory to array.
   * @param statusHistory statusHistory to add
   * @return the statusHistory
   */
  public StatusHistory addStatusHistory(StatusHistory statusHistory) {
    if (getStatusHistory() == null) {
      setStatusHistory(new ArrayList<StatusHistory>());
    }
    getStatusHistory().add(statusHistory);
    statusHistory.setRecipient(this);

    return statusHistory;
  }

  /**
   * Removes statusHistory from array.
   * @param statusHistory statusHistory to remove
   * @return the statusHistory
   */
  public StatusHistory removeStatusHistory(StatusHistory statusHistory) {
    getStatusHistory().remove(statusHistory);
    statusHistory.setRecipient(null);

    return statusHistory;
  }

  /**
   * Returns the recipientId.
   *
   * @return the recipientId
   */
  public Long getRecipientId() {
    return recipientId;
  }

  /**
   * Sets the recipientId.
   *
   * @param recipientId the recipientId to set
   */
  public void setRecipientId(Long recipientId) {
    this.recipientId = recipientId;
  }

  /**
   * Returns the structuralUnit.
   *
   * @return the structuralUnit
   */
  public String getStructuralUnit() {
    return structuralUnit;
  }

  /**
   * Sets the structuralUnit.
   *
   * @param structuralUnit the structuralUnit to set
   */
  public void setStructuralUnit(String structuralUnit) {
    this.structuralUnit = structuralUnit;
  }

  /**
   * Returns the dhxExternalConsignmentId.
   *
   * @return the dhxExternalConsignmentId
   */
  public String getDhxExternalConsignmentId() {
    return dhxExternalConsignmentId;
  }

  /**
   * Sets the dhxExternalConsignmentId.
   *
   * @param dhxExternalConsignmentId the dhxExternalConsignmentId to set
   */
  public void setDhxExternalConsignmentId(String dhxExternalConsignmentId) {
    this.dhxExternalConsignmentId = dhxExternalConsignmentId;
  }

  /**
   * Returns the dhxExternalReceiptId.
   *
   * @return the dhxExternalReceiptId
   */
  public String getDhxExternalReceiptId() {
    return dhxExternalReceiptId;
  }

  /**
   * Sets the dhxExternalReceiptId.
   *
   * @param dhxExternalReceiptId the dhxExternalReceiptId to set
   */
  public void setDhxExternalReceiptId(String dhxExternalReceiptId) {
    this.dhxExternalReceiptId = dhxExternalReceiptId;
  }

  /**
   * Returns the dhxInternalConsignmentId.
   *
   * @return the dhxInternalConsignmentId
   */
  public String getDhxInternalConsignmentId() {
    return dhxInternalConsignmentId;
  }

  /**
   * Sets the dhxInternalConsignmentId.
   *
   * @param dhxInternalConsignmentId the dhxInternalConsignmentId to set
   */
  public void setDhxInternalConsignmentId(String dhxInternalConsignmentId) {
    this.dhxInternalConsignmentId = dhxInternalConsignmentId;
  }

  /**
   * Returns the dokIdTeisesServeris.
   *
   * @return the dokIdTeisesServeris
   */
  public Integer getDokIdTeisesServeris() {
    return dokIdTeisesServeris;
  }

  /**
   * Sets the dokIdTeisesServeris.
   *
   * @param dokIdTeisesServeris the dokIdTeisesServeris to set
   */
  public void setDokIdTeisesServeris(Integer dokIdTeisesServeris) {
    this.dokIdTeisesServeris = dokIdTeisesServeris;
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
   * Returns the personalcode.
   *
   * @return the personalcode
   */
  public String getPersonalcode() {
    return personalcode;
  }

  /**
   * Sets the personalcode.
   *
   * @param personalcode the personalcode to set
   */
  public void setPersonalcode(String personalcode) {
    this.personalcode = personalcode;
  }

  /**
   * Returns the lastSendDate.
   *
   * @return the lastSendDate
   */
  public Timestamp getLastSendDate() {
    return lastSendDate;
  }

  /**
   * Sets the lastSendDate.
   *
   * @param lastSendDate the lastSendDate to set
   */
  public void setLastSendDate(Timestamp lastSendDate) {
    this.lastSendDate = lastSendDate;
  }

  /**
   * Returns the sendingStart.
   *
   * @return the sendingStart
   */
  public Timestamp getSendingStart() {
    return sendingStart;
  }

  /**
   * Sets the sendingStart.
   *
   * @param sendingStart the sendingStart to set
   */
  public void setSendingStart(Timestamp sendingStart) {
    this.sendingStart = sendingStart;
  }

  /**
   * Returns the sendingEnd.
   *
   * @return the sendingEnd
   */
  public Timestamp getSendingEnd() {
    return sendingEnd;
  }

  /**
   * Sets the sendingEnd.
   *
   * @param sendingEnd the sendingEnd to set
   */
  public void setSendingEnd(Timestamp sendingEnd) {
    this.sendingEnd = sendingEnd;
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
   * Returns the statusHistory.
   *
   * @return the statusHistory
   */
  public List<StatusHistory> getStatusHistory() {
    return statusHistory;
  }

  /**
   * Sets the statusHistory.
   *
   * @param statusHistory the statusHistory to set
   */
  public void setStatusHistory(List<StatusHistory> statusHistory) {
    this.statusHistory = statusHistory;
  }

  /**
   * Returns the organisation.
   *
   * @return the organisation
   */
  public Organisation getOrganisation() {
    return organisation;
  }

  /**
   * Sets the organisation.
   *
   * @param organisation the organisation to set
   */
  public void setOrganisation(Organisation organisation) {
    this.organisation = organisation;
  }

  /**
   * Returns the sendingTypeId.
   *
   * @return the sendingTypeId
   */
  public Integer getSendingTypeId() {
    return sendingTypeId;
  }

  /**
   * Sets the sendingTypeId.
   *
   * @param sendingTypeId the sendingTypeId to set
   */
  public void setSendingTypeId(Integer sendingTypeId) {
    this.sendingTypeId = sendingTypeId;
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
   * Returns the transport.
   *
   * @return the transport
   */
  public Transport getTransport() {
    return transport;
  }

  /**
   * Sets the transport.
   *
   * @param transport the transport to set
   */
  public void setTransport(Transport transport) {
    this.transport = transport;
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

}

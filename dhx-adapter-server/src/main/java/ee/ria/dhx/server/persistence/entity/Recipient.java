package ee.ria.dhx.server.persistence.entity;

import lombok.Getter;
import lombok.Setter;

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
@Getter
@Setter
public class Recipient extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "vastuvotja_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long recipientId;

  @Column(name = "allyksus")
  private String struCturalUnit;

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

  // bi-directional many-to-one association to VastuvotjaStaatus
  @ManyToOne
  @JoinColumn(name = "vastuvotja_staatus_id")
  private RecipientStatus recipientStatus;

  private String metaxml;

  public Recipient() {}

  @PrePersist
  public void beforeInsert() {
    insertHistory();
  }

  @PreUpdate
  public void beforeUpdate() {
    insertHistory();
  }

  private void insertHistory() {
    StatusHistory history = new StatusHistory();
    if (this.getRecipientStatus() != null) {
      history.setRecipientStatusId(this.getRecipientStatus().getRecipientStatusId());
    }
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

  public StatusHistory addStatusHistory(StatusHistory statusHistory) {
    if (getStatusHistory() == null) {
      setStatusHistory(new ArrayList<StatusHistory>());
    }
    getStatusHistory().add(statusHistory);
    statusHistory.setRecipient(this);

    return statusHistory;
  }

  public StatusHistory removeStatusHistory(StatusHistory statusHistory) {
    getStatusHistory().remove(statusHistory);
    statusHistory.setRecipient(null);

    return statusHistory;
  }

}

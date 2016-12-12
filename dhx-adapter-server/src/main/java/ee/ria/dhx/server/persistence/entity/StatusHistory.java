package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

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


/**
 * The persistent class for the staatuse_ajalugu database table.
 * 
 */
@Entity
@Table(name = "staatuse_ajalugu")
@Getter
@Setter
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

  @Column(name = "fault_detail")
  private String faultDetail;

  @Column(name = "fault_string")
  private String faultString;

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

}

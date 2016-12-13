package ee.ria.dhx.server.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * The persistent class for the saatja database table.
 * 
 */
@Entity
@Table(name = "saatja")
@Getter
@Setter
public class Sender extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "saatja_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer senderId;

  // bi-directional many-to-one association to Asutus
  @ManyToOne
  @JoinColumn(name = "asutus_id")
  private Organisation organisation;

  @Column(name = "isikukood")
  private String personalCode;

  @Column(name = "allyksus")
  private String structuralUnit;

  // bi-directional many-to-one association to Transport
  @ManyToOne
  @JoinColumn(name = "transport_id")
  private Transport transport;

  public Sender() {}


}

package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.Table;

/**
 * The persistent class for the saatja database table.
 * 
 */
/*// TODO: Make eager loading work
@NamedEntityGraph(
        name = "senders[organisation]",
        includeAllAttributes = true,
        subgraphs = {
                @NamedSubgraph(
                        name = "organisation",
                        attributeNodes = {
                                @NamedAttributeNode("name"),
                                @NamedAttributeNode("xroadInstance"),
                                @NamedAttributeNode("memberClass"),
                                @NamedAttributeNode("registrationCode"),
                                @NamedAttributeNode("subSystem"),
                                @NamedAttributeNode("isActive")
                        })
        }
)*/
@Entity
@Table(name = "saatja")
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

  /**
   * Returns the senderId.
   *
   * @return the senderId
   */
  public Integer getSenderId() {
    return senderId;
  }

  /**
   * Sets the senderId.
   *
   * @param senderId the senderId to set
   */
  public void setSenderId(Integer senderId) {
    this.senderId = senderId;
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
   * Returns the personalCode.
   *
   * @return the personalCode
   */
  public String getPersonalCode() {
    return personalCode;
  }

  /**
   * Sets the personalCode.
   *
   * @param personalCode the personalCode to set
   */
  public void setPersonalCode(String personalCode) {
    this.personalCode = personalCode;
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Sender [senderId=" + senderId + ", organisation=" + organisation + ", personalCode="
        + personalCode + ", structuralUnit=" + structuralUnit + "]";
  }

}

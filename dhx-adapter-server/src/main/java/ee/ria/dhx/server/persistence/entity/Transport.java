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
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The persistent class for the transport database table.
 * 
 */
/*// TODO: Make eager loading work
@NamedEntityGraph(
        name = "transport[dokument, senders[organisation]]",
        includeAllAttributes = true,
        subgraphs = {
                @NamedSubgraph(name = "dokument", attributeNodes = {
                        @NamedAttributeNode(value = "documentId"),
                        @NamedAttributeNode(value = "outgoingDocument"),
                        @NamedAttributeNode(value = "content"),
                        @NamedAttributeNode(value = "dateCreated")
                }),
                @NamedSubgraph(name = "senders[organisation]", attributeNodes = {
                        @NamedAttributeNode(value = "organisation")
                })
        }
)*/
@Entity
@Table(name = "transport")
public class Transport extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "transport_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer transportId;

  @Column(name = "saatmise_algus")
  private Timestamp sendingStart;

  @Column(name = "saatmise_lopp")
  private Timestamp sendingEnd;

  // bi-directional many-to-one association to Saatja
  @OneToMany(mappedBy = "transport", cascade = {CascadeType.ALL})
  private List<Sender> senders;

  // bi-directional many-to-one association to Dokument
  @ManyToOne
  @JoinColumn(name = "dokument_id")
  private Document dokument;

  // bi-directional many-to-one association to Klassifikaator
  // @ManyToOne
  @Column(name = "staatus_id")
  private Integer statusId;

  // bi-directional many-to-one association to Vastuvotja
  @OneToMany(mappedBy = "transport", cascade = {CascadeType.ALL})
  private List<Recipient> recipients;

  public Transport() {}

  /**
   * Adds sender to array.
   * 
   * @param sender sender to add
   * @return the sender
   */
  public Sender addSender(Sender sender) {
    if (getSenders() == null) {
      setSenders(new ArrayList<Sender>());
    }
    getSenders().add(sender);
    sender.setTransport(this);

    return sender;
  }

  /**
   * Removes sender from array.
   * 
   * @param sender sender to remove
   * @return the sender
   */
  public Sender removeSender(Sender sender) {
    getSenders().remove(sender);
    sender.setTransport(null);

    return sender;
  }

  /**
   * Adds recipient to array.
   * 
   * @param recipient recipient to add
   * @return the recipient
   */
  public Recipient addRecipient(Recipient recipient) {
    if (getRecipients() == null) {
      setRecipients(new ArrayList<Recipient>());
    }
    getRecipients().add(recipient);
    recipient.setTransport(this);

    return recipient;
  }

  /**
   * Removes recipient from array.
   * 
   * @param recipient recipient to remove
   * @return the recipient
   */
  public Recipient removeVastuvotja(Recipient recipient) {
    getRecipients().remove(recipient);
    recipient.setTransport(null);

    return recipient;
  }

  /**
   * Returns the transportId.
   *
   * @return the transportId
   */
  public Integer getTransportId() {
    return transportId;
  }

  /**
   * Sets the transportId.
   *
   * @param transportId the transportId to set
   */
  public void setTransportId(Integer transportId) {
    this.transportId = transportId;
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
   * Returns the senders.
   *
   * @return the senders
   */
  public List<Sender> getSenders() {
    return senders;
  }

  /**
   * Sets the senders.
   *
   * @param senders the senders to set
   */
  public void setSenders(List<Sender> senders) {
    this.senders = senders;
  }

  /**
   * Returns the dokument.
   *
   * @return the dokument
   */
  public Document getDokument() {
    return dokument;
  }

  /**
   * Sets the dokument.
   *
   * @param dokument the dokument to set
   */
  public void setDokument(Document dokument) {
    this.dokument = dokument;
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
   * Returns the recipients.
   *
   * @return the recipients
   */
  public List<Recipient> getRecipients() {
    return recipients;
  }

  /**
   * Sets the recipients.
   *
   * @param recipients the recipients to set
   */
  public void setRecipients(List<Recipient> recipients) {
    this.recipients = recipients;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Transport [transportId=" + transportId + ", sendingStart=" + sendingStart
        + ", sendingEnd=" + sendingEnd + ", senders=" + senders + ", statusId=" + statusId
        + ", recipients=" + recipients + "]";
  }

}

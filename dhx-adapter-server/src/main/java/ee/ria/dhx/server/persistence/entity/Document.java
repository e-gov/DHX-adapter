package ee.ria.dhx.server.persistence.entity;

import org.hibernate.annotations.Type;

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
import javax.persistence.Table;
import javax.persistence.Lob;

/**
 * The persistent class for the document database table.
 *
 */
@Entity
@Table(name = "dokument")
public class Document extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "dokument_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long documentId;

  @Column(name = "guid")
  private String guid;

  @Column(name = "kapsli_versioon")
  private String capsuleVersion;

  @Column(name = "sailitustahtaeg")
  private Timestamp storageDeadline;

  @Lob
  @Type(type = "org.hibernate.type.MaterializedClobType")
  @Column(name = "sisu")
  private String content;

  @Column(name = "suurus")
  private Long size;

  @Column(name = "versioon")
  private Integer containerVersion;

  // bi-directional many-to-one association to Asutus
  @ManyToOne
  @JoinColumn(name = "asutus_id")
  private Organisation organisation;

  // bi-directional many-to-one association to Kaust
  @ManyToOne
  @JoinColumn(name = "kaust_id")
  private Folder folder;

  // bi-directional many-to-one association to Transport
  @OneToMany(mappedBy = "dokument", cascade = {CascadeType.ALL})
  private List<Transport> transports;

  @Column(name = "outgoing_document")
  private Boolean outgoingDocument;

  public Document() {}

  /**
   * Adds the transport to the array.
   * 
   * @param transport transport to add
   * @return the transport
   */
  public Transport addTransport(Transport transport) {
    if (getTransports() == null) {
      setTransports(new ArrayList<Transport>());
    }
    getTransports().add(transport);
    transport.setDokument(this);

    return transport;
  }

  /**
   * Remove the transport from array.
   * 
   * @param transport transport to remove
   * @return the transport
   */
  public Transport removeTransport(Transport transport) {
    getTransports().remove(transport);
    transport.setDokument(null);

    return transport;
  }

  /**
   * Returns the documentId.
   *
   * @return the documentId
   */
  public Long getDocumentId() {
    return documentId;
  }

  /**
   * Sets the documentId.
   *
   * @param documentId the documentId to set
   */
  public void setDocumentId(Long documentId) {
    this.documentId = documentId;
  }

  /**
   * Returns the guid.
   *
   * @return the guid
   */
  public String getGuid() {
    return guid;
  }

  /**
   * Sets the guid.
   *
   * @param guid the guid to set
   */
  public void setGuid(String guid) {
    this.guid = guid;
  }

  /**
   * Returns the capsuleVersion that is used in this document.
   *
   * @return the capsuleVersion
   */
  public String getCapsuleVersion() {
    return capsuleVersion;
  }

  /**
   * Sets the capsuleVersion that is used in this document.
   *
   * @param capsuleVersion the capsuleVersion to set
   */
  public void setCapsuleVersion(String capsuleVersion) {
    this.capsuleVersion = capsuleVersion;
  }

  /**
   * Returns the storageDeadline.
   *
   * @return the storageDeadline
   */
  public Timestamp getStorageDeadline() {
    return storageDeadline;
  }

  /**
   * Sets the storageDeadline.
   *
   * @param storageDeadline the storageDeadline to set
   */
  public void setStorageDeadline(Timestamp storageDeadline) {
    this.storageDeadline = storageDeadline;
  }

  /**
   * Returns the content.
   *
   * @return the content
   */
  public String getContent() {
    return content;
  }

  /**
   * Sets the content.
   *
   * @param content the content to set
   */
  public void setContent(String content) {
    this.content = content;
  }

  /**
   * Returns the size.
   *
   * @return the size
   */
  public Long getSize() {
    return size;
  }

  /**
   * Sets the size.
   *
   * @param size the size to set
   */
  public void setSize(Long size) {
    this.size = size;
  }

  /**
   * Returns the containerVersion.
   *
   * @return the containerVersion
   */
  public Integer getContainerVersion() {
    return containerVersion;
  }

  /**
   * Sets the containerVersion.
   *
   * @param containerVersion the containerVersion to set
   */
  public void setContainerVersion(Integer containerVersion) {
    this.containerVersion = containerVersion;
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
   * Returns the folder.
   *
   * @return the folder
   */
  public Folder getFolder() {
    return folder;
  }

  /**
   * Sets the folder.
   *
   * @param folder the folder to set
   */
  public void setFolder(Folder folder) {
    this.folder = folder;
  }

  /**
   * Returns the transports.
   *
   * @return the transports
   */
  public List<Transport> getTransports() {
    return transports;
  }

  /**
   * Sets the transports.
   *
   * @param transports the transports to set
   */
  public void setTransports(List<Transport> transports) {
    this.transports = transports;
  }

  /**
   * Returns the outgoingDocument. If outgoingDocument is false, that means that the document is
   * incoming.
   *
   * @return the outgoingDocument
   */
  public Boolean getOutgoingDocument() {
    return outgoingDocument;
  }

  /**
   * Sets the outgoingDocument. If outgoingDocument is false, that means that the document is
   * incoming.
   *
   * @param outgoingDocument the outgoingDocument to set
   */
  public void setOutgoingDocument(Boolean outgoingDocument) {
    this.outgoingDocument = outgoingDocument;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Document [documentId=" + documentId + ", guid=" + guid + ", capsuleVersion="
        + capsuleVersion + ", storageDeadline=" + storageDeadline + ", size=" + size
        + ", containerVersion=" + containerVersion + ", organisation=" + organisation
        + ", folder=" + folder + ", transports=" + transports + ", outgoingDocument="
        + outgoingDocument + "]";
  }


}

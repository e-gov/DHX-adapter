package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the dokument database table.
 * 
 */
@Entity
@Table(name = "dokument")
@Getter
@Setter
public class Document extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "dokument_id")
  @GeneratedValue(strategy=GenerationType.AUTO)
  private Integer documentId;

  private String guid;

  @Column(name = "kapsli_versioon")
  private String capsuleVersion;

  @Column(name = "sailitustahtaeg")
  private Timestamp storageDeadline;

  @Column(name = "sisu", columnDefinition = "TEXT")
  private String content;

  @Column(name = "suurus")
  private Long size;

  @Column(name = "versioon")
  private Integer containerVersion;



  // bi-directional many-to-one association to DokumendiAjalugu
  @OneToMany(mappedBy = "dokument")
  private List<DokumendiAjalugu> documentHistorys;

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
  
  private Boolean outgoingDocument;

  public Document() {}

  public Transport addTransport(Transport transport) {
    if(getTransports() == null) {
      setTransports(new ArrayList<Transport>());
    }
    getTransports().add(transport);
    transport.setDokument(this);

    return transport;
  }

  public Transport removeTransport(Transport transport) {
    getTransports().remove(transport);
    transport.setDokument(null);

    return transport;
  }

  public DokumendiAjalugu addDocumentHistory(DokumendiAjalugu documentHistory) {
    if(getDocumentHistorys() == null) {
      setDocumentHistorys(new ArrayList<DokumendiAjalugu>());
    }
    getDocumentHistorys().add(documentHistory);
    documentHistory.setDokument(this);

    return documentHistory;
  }

  public DokumendiAjalugu removeTransport(DokumendiAjalugu documentHistory) {
    getDocumentHistorys().remove(documentHistory);
    documentHistory.setDokument(null);
    return documentHistory;
  }

}

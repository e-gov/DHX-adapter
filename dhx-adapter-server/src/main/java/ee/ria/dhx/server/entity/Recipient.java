package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the vastuvotja database table.
 * 
 */
@Entity
@Table(name = "vastuvotja")
@Getter
@Setter
public class Recipient  extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="vastuvotja_id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long recipientId;

	@Column(name="allyksus")
	private String struCturalUnit;

	@Column(name="dhx_external_consignment_id")
	private String dhxExternalConsignmentId;

	@Column(name="dhx_external_receipt_id")
	private String dhxExternalReceiptId;

	@Column(name="dhx_internal_consignment_id")
	private String dhxInternalConsignmentId;

	@Column(name="dok_id_teises_serveris")
	private Integer dokIdTeisesServeris;

	@Column(name="fault_actor")
	private String faultActor;

	@Column(name="fault_code")
	private String faultCode;

	@Column(name="fault_detail", columnDefinition = "TEXT")
	private String faultDetail;

	@Column(name = "fault_string", columnDefinition = "TEXT")
	private String faultString;

	@Column(name="isikukood")
	private String personalcode;

	@Column(name="last_send_date")
	private Timestamp lastSendDate;

	@Column(name="saatmise_algus")
	private Timestamp sendingStart;

	@Column(name="saatmise_lopp")
	private Timestamp sendingEnd;

	//bi-directional many-to-one association to StaatuseAjalugu
	@OneToMany(mappedBy="vastuvotja")
	private List<StaatuseAjalugu> statusHistory;

	//bi-directional many-to-one association to Asutus
	@ManyToOne
	@JoinColumn(name="asutus_id")
	private Organisation organisation;

	//bi-directional many-to-one association to Klassifikaator
	@ManyToOne
	@JoinColumn(name="saatmisviis_id")
	private Classificator sendingType;

	//bi-directional many-to-one association to Klassifikaator
	@ManyToOne
	@JoinColumn(name="staatus_id")
	private Classificator status;

	//bi-directional many-to-one association to Transport
	@ManyToOne
	@JoinColumn(name="transport_id")
	private Transport transport;

	//bi-directional many-to-one association to VastuvotjaStaatus
	@ManyToOne
	@JoinColumn(name="vastuvotja_staatus_id")
	private RecipientStatus recipientStatus;

	public Recipient() {
	}


	public StaatuseAjalugu addStatusHistory(StaatuseAjalugu statusHistory) {
		getStatusHistory().add(statusHistory);
		statusHistory.setVastuvotja(this);

		return statusHistory;
	}

	public StaatuseAjalugu removeStatusHistory(StaatuseAjalugu statusHistory) {
	  getStatusHistory().remove(statusHistory);
		statusHistory.setVastuvotja(null);

		return statusHistory;
	}

}
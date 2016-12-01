package ee.ria.dhx.server.entity;

import java.io.Serializable;

import javax.persistence.*;

import java.sql.Timestamp;


/**
 * The persistent class for the staatuse_ajalugu database table.
 * 
 */
@Entity
@Table(name="staatuse_ajalugu")
@NamedQuery(name="StaatuseAjalugu.findAll", query="SELECT s FROM StaatuseAjalugu s")
public class StaatuseAjalugu implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="staatuse_ajalugu_id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer staatuseAjaluguId;

	@Column(name="fault_actor")
	private String faultActor;

	@Column(name="fault_code")
	private String faultCode;

	@Column(name="fault_detail")
	private String faultDetail;

	@Column(name="fault_string")
	private String faultString;

	private String metaxml;

	@Column(name="staatuse_muutmise_aeg")
	private Timestamp staatuseMuutmiseAeg;

	@Column(name="vastuvotja_staatus_id")
	private Integer vastuvotjaStaatusId;

	//bi-directional many-to-one association to Klassifikaator
	@ManyToOne
	@JoinColumn(name="staatus_id")
	private Classificator klassifikaator;

	//bi-directional many-to-one association to Vastuvotja
	@ManyToOne
	@JoinColumn(name="vastuvotja_id")
	private Recipient vastuvotja;

	public StaatuseAjalugu() {
	}

	public Integer getStaatuseAjaluguId() {
		return this.staatuseAjaluguId;
	}

	public void setStaatuseAjaluguId(Integer staatuseAjaluguId) {
		this.staatuseAjaluguId = staatuseAjaluguId;
	}

	public String getFaultActor() {
		return this.faultActor;
	}

	public void setFaultActor(String faultActor) {
		this.faultActor = faultActor;
	}

	public String getFaultCode() {
		return this.faultCode;
	}

	public void setFaultCode(String faultCode) {
		this.faultCode = faultCode;
	}

	public String getFaultDetail() {
		return this.faultDetail;
	}

	public void setFaultDetail(String faultDetail) {
		this.faultDetail = faultDetail;
	}

	public String getFaultString() {
		return this.faultString;
	}

	public void setFaultString(String faultString) {
		this.faultString = faultString;
	}

	public String getMetaxml() {
		return this.metaxml;
	}

	public void setMetaxml(String metaxml) {
		this.metaxml = metaxml;
	}

	public Timestamp getStaatuseMuutmiseAeg() {
		return this.staatuseMuutmiseAeg;
	}

	public void setStaatuseMuutmiseAeg(Timestamp staatuseMuutmiseAeg) {
		this.staatuseMuutmiseAeg = staatuseMuutmiseAeg;
	}

	public Integer getVastuvotjaStaatusId() {
		return this.vastuvotjaStaatusId;
	}

	public void setVastuvotjaStaatusId(Integer vastuvotjaStaatusId) {
		this.vastuvotjaStaatusId = vastuvotjaStaatusId;
	}

	public Classificator getKlassifikaator() {
		return this.klassifikaator;
	}

	public void setKlassifikaator(Classificator klassifikaator) {
		this.klassifikaator = klassifikaator;
	}

	public Recipient getVastuvotja() {
		return this.vastuvotja;
	}

	public void setVastuvotja(Recipient vastuvotja) {
		this.vastuvotja = vastuvotja;
	}

}
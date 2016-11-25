package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the asutus database table.
 * 
 */
@Entity
@Table(name="asutus")
@Getter
@Setter
public class Organisation  extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="asutus_id")
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer organisationId;

	
	@Column(name="dhl_saatmine")
	private Boolean isActive;

	@Column(name="kapsel_versioon")
	private String capsuleVersion;

	@Column(name="nimetus")
	private String name;

	@Column(name="registrikood")
	private String registrationCode;

	@Column(name="subsystem")
	private String subSystem;
	
	@Column(name="member_class")
	private String memberClass;
	
	@Column(name="xroad_instance")
	private String xroadInstance;
	
	@Column(name="representee_start")
	private Timestamp representeeStart;
	
	@Column(name="representee_end")
	private Timestamp representeeEnd;

	
	//bi-directional many-to-one association to Organisation
	@ManyToOne
	@JoinColumn(name="vahendaja_asutus_id")
	private Organisation representor;

	//bi-directional many-to-one association to Organisation
	@OneToMany(mappedBy="representor")
	private List<Organisation> representees;

	//bi-directional many-to-one association to Dokument
	/*@OneToMany(mappedBy="asutus")
	private List<Dokument> dokuments;*/


	public Organisation() {
	}


	public Organisation addRepresentee(Organisation organisation) {
		getRepresentees().add(organisation);
		organisation.setRepresentor(this);

		return organisation;
	}

	public Organisation removeRepresentee(Organisation organisation) {
	  getRepresentees().remove(organisation);
	  organisation.setRepresentor(null);
		return organisation;
	}

}
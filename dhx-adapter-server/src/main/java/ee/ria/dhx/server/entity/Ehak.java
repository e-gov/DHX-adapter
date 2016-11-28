package ee.ria.dhx.server.entity;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the ehak database table.
 * 
 */
@Entity
@NamedQuery(name="Ehak.findAll", query="SELECT e FROM Ehak e")
public class Ehak implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="ehak_id")
	private String ehakId;

	private Timestamp created;

	@Column(name="last_modified")
	private Timestamp lastModified;

	private String maakond;

	private String nimi;

	private String roopnimi;

	private String tyyp;

	private String username;

	private String vald;

	public Ehak() {
	}

	public String getEhakId() {
		return this.ehakId;
	}

	public void setEhakId(String ehakId) {
		this.ehakId = ehakId;
	}

	public Timestamp getCreated() {
		return this.created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Timestamp getLastModified() {
		return this.lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}

	public String getMaakond() {
		return this.maakond;
	}

	public void setMaakond(String maakond) {
		this.maakond = maakond;
	}

	public String getNimi() {
		return this.nimi;
	}

	public void setNimi(String nimi) {
		this.nimi = nimi;
	}

	public String getRoopnimi() {
		return this.roopnimi;
	}

	public void setRoopnimi(String roopnimi) {
		this.roopnimi = roopnimi;
	}

	public String getTyyp() {
		return this.tyyp;
	}

	public void setTyyp(String tyyp) {
		this.tyyp = tyyp;
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVald() {
		return this.vald;
	}

	public void setVald(String vald) {
		this.vald = vald;
	}

}
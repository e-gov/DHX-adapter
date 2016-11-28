package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the klassifikaatori_tyyp database table.
 * 
 */
@Entity
@Table(name="klassifikaatori_tyyp")
@Getter
@Setter
public class KlassifikaatoriTyyp implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="klassifikaatori_tyyp_id")
	private Integer klassifikaatoriTyypId;

	@Column(name="nimetus")
	private String name;


	public KlassifikaatoriTyyp() {
	}


}
package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the klassifikaator database table.
 * 
 */
@Entity
@Table(name="klassifikaator")
@Setter
@Getter
public class Classificator  extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="klassifikaator_id")
	private Integer klassifikaatorId;

	@Column(name="nimetus")
	private String name;

	//bi-directional many-to-one association to KlassifikaatoriTyyp
	@ManyToOne
	@JoinColumn(name="klassifikaatori_tyyp_id")
	private KlassifikaatoriTyyp klassifikaatoriTyyp;

	public Classificator() {
	}

}
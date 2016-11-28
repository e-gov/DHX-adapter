package ee.ria.dhx.server.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.*;

import java.util.List;


/**
 * The persistent class for the vastuvotja_staatus database table.
 * 
 */
@Entity
@Table(name="vastuvotja_staatus")
@Getter
@Setter
public class RecipientStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="vastuvotja_staatus_id")
	private Integer recipientStatusId;

	@Column(name="nimetus")
	private String name;

	public RecipientStatus() {
	}

}
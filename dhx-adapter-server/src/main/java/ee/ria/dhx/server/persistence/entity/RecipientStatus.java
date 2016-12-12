package ee.ria.dhx.server.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


/**
 * The persistent class for the vastuvotja_staatus database table.
 * 
 */
@Entity
@Table(name = "vastuvotja_staatus")
@Getter
@Setter
public class RecipientStatus implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "vastuvotja_staatus_id")
  private Integer recipientStatusId;

  @Column(name = "nimetus")
  private String name;

  public RecipientStatus() {}

}

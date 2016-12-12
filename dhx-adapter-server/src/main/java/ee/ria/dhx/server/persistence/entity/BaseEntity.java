package ee.ria.dhx.server.persistence.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

  Date dateCreated;
  Date dateModified;

  @Version
  @Column(name = "VERSION")
  private Integer version;

  @PrePersist
  public void beforeInsert() {
    dateCreated = new Date();
  }

  @PreUpdate
  public void beforeUpdate() {
    dateModified = new Date();
  }

}

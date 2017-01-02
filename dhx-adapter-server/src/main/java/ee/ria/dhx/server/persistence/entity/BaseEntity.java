package ee.ria.dhx.server.persistence.entity;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

/**
 * Genereic entity class that is super class for other entities.
 * 
 * @author Aleksei Kokarev
 *
 */

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

  /**
   * Returns the dateCreated.
   * 
   * @return the dateCreated
   */
  public Date getDateCreated() {
    return dateCreated;
  }

  /**
   * Sets the dateCreated.
   * 
   * @param dateCreated the dateCreated to set
   */
  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }

  /**
   * Returns the dateModified.
   * 
   * @return the dateModified
   */
  public Date getDateModified() {
    return dateModified;
  }

  /**
   * Sets the dateModified.
   * 
   * @param dateModified the dateModified to set
   */
  public void setDateModified(Date dateModified) {
    this.dateModified = dateModified;
  }

  /**
   * Returns the version.
   * 
   * @return the version
   */
  public Integer getVersion() {
    return version;
  }

  /**
   * Sets the version.
   * 
   * @param version the version to set
   */
  public void setVersion(Integer version) {
    this.version = version;
  }

}

package ee.ria.dhx.server.persistence.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * The persistent class for the asutus database table.
 * 
 */
@Entity
@Table(name = "asutus", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"registrikood", "subsystem"})})
public class Organisation extends BaseEntity implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "asutus_id")
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Integer organisationId;

  @Column(name = "dhl_saatmine")
  private Boolean isActive;

  @Column(name = "kapsel_versioon")
  private String capsuleVersion;

  @Column(name = "nimetus")
  private String name;

  @Column(name = "registrikood")
  private String registrationCode;

  @Column(name = "subsystem")
  private String subSystem;

  @Column(name = "member_class")
  private String memberClass;

  @Column(name = "xroad_instance")
  private String xroadInstance;

  @Column(name = "representee_start")
  private Timestamp representeeStart;

  @Column(name = "representee_end")
  private Timestamp representeeEnd;

  @Column(name = "dhx_asutus")
  private Boolean dhxOrganisation;

  @Column(name = "own_representee")
  private Boolean ownRepresentee;

  @Column(name = "reaalne_nimi")
  private String realName;

  // bi-directional many-to-one association to Organisation
  @ManyToOne
  @JoinColumn(name = "vahendaja_asutus_id")
  private Organisation representor;

  // bi-directional many-to-one association to Organisation
  @OneToMany(mappedBy = "representor")
  private List<Organisation> representees;

  // bi-directional many-to-one association to Dokument
  /*
   * @OneToMany(mappedBy="asutus") private List<Dokument> dokuments;
   */

  public Organisation() {}

  /**
   * Adds representee to array.
   * 
   * @param organisation organisation to add
   * @return the organisation
   */
  public Organisation addRepresentee(Organisation organisation) {
    getRepresentees().add(organisation);
    organisation.setRepresentor(this);

    return organisation;
  }

  /**
   * Removes organisation from array.
   * 
   * @param organisation organisation to remove
   * @return the organisation
   */
  public Organisation removeRepresentee(Organisation organisation) {
    getRepresentees().remove(organisation);
    organisation.setRepresentor(null);
    return organisation;
  }

  /**
   * Returns the organisationId.
   *
   * @return the organisationId
   */
  public Integer getOrganisationId() {
    return organisationId;
  }

  /**
   * Sets the organisationId.
   *
   * @param organisationId the organisationId to set
   */
  public void setOrganisationId(Integer organisationId) {
    this.organisationId = organisationId;
  }

  /**
   * Returns the isActive.
   *
   * @return the isActive
   */
  public Boolean getIsActive() {
    return isActive;
  }

  /**
   * Sets the isActive.
   *
   * @param isActive the isActive to set
   */
  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  /**
   * Returns the capsuleVersion that is used by this organisation.
   *
   * @return the capsuleVersion
   */
  public String getCapsuleVersion() {
    return capsuleVersion;
  }

  /**
   * Sets the capsuleVersion that is used by this organsaiotn.
   *
   * @param capsuleVersion the capsuleVersion to set
   */
  public void setCapsuleVersion(String capsuleVersion) {
    this.capsuleVersion = capsuleVersion;
  }

  /**
   * Returns the name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns the registrationCode.
   *
   * @return the registrationCode
   */
  public String getRegistrationCode() {
    return registrationCode;
  }

  /**
   * Sets the registrationCode.
   *
   * @param registrationCode the registrationCode to set
   */
  public void setRegistrationCode(String registrationCode) {
    this.registrationCode = registrationCode;
  }

  /**
   * Returns the subSystem(either Xroad member's subsystemCode or representees system).
   *
   * @return the subSystem
   */
  public String getSubSystem() {
    return subSystem;
  }

  /**
   * Sets the subSystem(either Xroad member's subsystemCode or representees system).
   *
   * @param subSystem the subSystem to set
   */
  public void setSubSystem(String subSystem) {
    this.subSystem = subSystem;
  }

  /**
   * Returns the Xroad member's memberClass.
   *
   * @return the memberClass
   */
  public String getMemberClass() {
    return memberClass;
  }

  /**
   * Sets the Xroad member's memberClass.
   *
   * @param memberClass the memberClass to set
   */
  public void setMemberClass(String memberClass) {
    this.memberClass = memberClass;
  }

  /**
   * Returns the xroadInstance.
   *
   * @return the xroadInstance
   */
  public String getXroadInstance() {
    return xroadInstance;
  }

  /**
   * Sets the xroadInstance.
   *
   * @param xroadInstance the xroadInstance to set
   */
  public void setXroadInstance(String xroadInstance) {
    this.xroadInstance = xroadInstance;
  }

  /**
   * Returns the representeeStart(start date when possible to send to given representee).
   *
   * @return the representeeStart
   */
  public Timestamp getRepresenteeStart() {
    return representeeStart;
  }

  /**
   * Sets the representeeStart(start date when possible to send to given representee).
   *
   * @param representeeStart the representeeStart to set
   */
  public void setRepresenteeStart(Timestamp representeeStart) {
    this.representeeStart = representeeStart;
  }

  /**
   * Returns the representeeEnd(end date when possible to send to given representee).
   *
   * @return the representeeEnd
   */
  public Timestamp getRepresenteeEnd() {
    return representeeEnd;
  }

  /**
   * Sets the representeeEnd(end date when possible to send to given representee).
   *
   * @param representeeEnd the representeeEnd to set
   */
  public void setRepresenteeEnd(Timestamp representeeEnd) {
    this.representeeEnd = representeeEnd;
  }

  /**
   * Returns the dhxOrganisation. Defines whether given organisation is member of DHX.
   *
   * @return the dhxOrganisation
   */
  public Boolean getDhxOrganisation() {
    return dhxOrganisation;
  }

  /**
   * Sets the dhxOrganisation. Defines whether given organisation is member of DHX.
   *
   * @param dhxOrganisation the dhxOrganisation to set
   */
  public void setDhxOrganisation(Boolean dhxOrganisation) {
    this.dhxOrganisation = dhxOrganisation;
  }

  /**
   * Returns the ownRepresentee. Defines whether given organisation is representee of the server
   * owner.
   *
   * @return the ownRepresentee
   */
  public Boolean getOwnRepresentee() {
    return ownRepresentee;
  }

  /**
   * Sets the ownRepresentee. Defines whether given organisation is representee of the server owner.
   *
   * @param ownRepresentee the ownRepresentee to set
   */
  public void setOwnRepresentee(Boolean ownRepresentee) {
    this.ownRepresentee = ownRepresentee;
  }

  /**
   * Returns the real name of the Organisation. By default it is empty, not empty if real
   * organisation name differs from the one found in Xroad parameters.
   *
   * @return the realName
   */
  public String getRealName() {
    return realName;
  }

  /**
   * Sets the real name of the Organisation. By default it is empty, not empty if real organisation
   * name differs from the one found in Xroad parameters.
   *
   * @param realName the realName to set
   */
  public void setRealName(String realName) {
    this.realName = realName;
  }

  /**
   * Returns the representor.
   *
   * @return the representor
   */
  public Organisation getRepresentor() {
    return representor;
  }

  /**
   * Sets the representor.
   *
   * @param representor the representor to set
   */
  public void setRepresentor(Organisation representor) {
    this.representor = representor;
  }

  /**
   * Returns the representees.
   *
   * @return the representees
   */
  public List<Organisation> getRepresentees() {
    return representees;
  }

  /**
   * Sets the representees.
   *
   * @param representees the representees to set
   */
  public void setRepresentees(List<Organisation> representees) {
    this.representees = representees;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Organisation [organisationId=" + organisationId + ", isActive=" + isActive
        + ", capsuleVersion=" + capsuleVersion + ", name=" + name + ", registrationCode="
        + registrationCode + ", subSystem=" + subSystem + ", memberClass=" + memberClass
        + ", xroadInstance=" + xroadInstance + ", representeeStart=" + representeeStart
        + ", representeeEnd=" + representeeEnd + ", dhxOrganisation=" + dhxOrganisation
        + ", ownRepresentee=" + ownRepresentee + ", representor=" + representor + "]";
  }


}

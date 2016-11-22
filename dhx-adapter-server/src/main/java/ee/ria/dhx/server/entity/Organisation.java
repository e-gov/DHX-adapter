package ee.ria.dhx.server.entity;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

@Entity
@Table(name="asutus")
@Getter
@Setter
public class Organisation extends BaseEntity{
  

  public Organisation () {
    
  }
  
  public Organisation(Long id, String memberCode, String memberClass, String name,
      String capsuleVersion, String subsystem, Boolean isActive, Organisation representor,
      List<Organisation> representees) {
    super();
    this.id = id;
    this.memberCode = memberCode;
    this.memberClass = memberClass;
    this.name = name;
    this.capsuleVersion = capsuleVersion;
    this.subsystem = subsystem;
    this.isActive = isActive;
    this.representor = representor;
   // this.representees = representees;
  }

  @Id
  @GeneratedValue(strategy=GenerationType.AUTO)
  @Column(nullable = false, name = "asutus_id")
  private Long id;
  
  @Column(nullable = false, name="registrikood")
  private String memberCode;
  
  @Column(nullable = true, name="liikme_klass")
  private String memberClass;
    
  @Column(nullable = false, name="nimetus")
  private String name;
  
  @Column(nullable = true, name="kapsel_versioon")
  private String capsuleVersion;
  
  @Column(nullable = true, name="registrikood2") 
  private String subsystem;
  
  @Column(nullable = false, name="dhl_saatmine") 
  private Boolean isActive;
  
  @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
  @JoinColumn(name="troop_fk")
  private Organisation representor;
  
  /*@OneToMany
  @JoinColumn(name="troop_fk") 
  private List<Organisation> representees;*/

  @Override
  public String toString() {
    return "Organisation [id=" + id + ", memberCode=" + memberCode + ", memberClass="
        + memberClass + ", name=" + name + ", capsuleVersion=" + capsuleVersion + ", subsystem="
        + subsystem + ", isActive=" + isActive + ", representor=" + representor
        + /*", representees=" + representees + */"]";
  }


}

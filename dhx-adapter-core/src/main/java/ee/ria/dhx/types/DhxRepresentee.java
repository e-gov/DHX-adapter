package ee.ria.dhx.types;

import ee.ria.dhx.exception.DhxException;

import ee.ria.dhx.util.ConversionUtil;

import ee.ria.dhx.types.eu.x_road.dhx.producer.Representee;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Representee object. Contains all information needed for representee.
 * 
 * @author Aleksei Kokarev
 *
 */
@Getter
@Setter
public class DhxRepresentee {

  private String representeeCode;
  private Date startDate;
  private Date endDate;

  private String representeeName;
  private String representeeSystem;

  /**
   * Create Representee from member(which is representationList service output).
   * 
   * @param representee - member from which to create representee
   */
  public DhxRepresentee(Representee representee) {
    this.representeeCode = representee.getMemberCode();
    if (representee.getStartDate() != null) {
      this.startDate = ConversionUtil.toDate(representee.getStartDate());
    }
    if (representee.getEndDate() != null) {
      this.endDate = ConversionUtil.toDate(representee.getEndDate());
    }
    if (representee.getRepresenteeName() != null) {
      this.representeeName = representee.getRepresenteeName();
    }
    if (representee.getRepresenteeSystem() != null) {
      this.representeeSystem = representee.getRepresenteeSystem();
    }
  }

  /**
   * Create Representee.
   * 
   * @param memberCode - X-road member code
   * @param startDate - representees start date
   * @param endDate - representees end date
   * @param name - representees name
   * @param system - representees internal system
   */
  public DhxRepresentee(String memberCode, Date startDate, Date endDate, String name,
      String system) {
    this.representeeCode = memberCode;
    this.startDate = startDate;
    this.endDate = endDate;
    this.representeeName = name;
    this.representeeSystem = system;
  }

  @Override
  public String toString() {
    return "memberCode: " + representeeCode + " startDate: " + startDate + " endDate: " + endDate
        + "name: " + representeeName + " system: " + representeeSystem;
  }

  /**
   * Converts representee to member(for representationList service output).
   * 
   * @return - member
   * @throws DhxException - thrown if error occurs while converting
   */
  public Representee convertToRepresentee() throws DhxException {
    Representee representee = new Representee();
    representee.setMemberCode(representeeCode);
    representee.setStartDate(ConversionUtil.toGregorianCalendar(this.getStartDate()));
    representee.setEndDate(ConversionUtil.toGregorianCalendar(this.getEndDate()));
    representee.setRepresenteeSystem(representeeSystem);
    representee.setRepresenteeName(representeeName);
    return representee;
  }


}

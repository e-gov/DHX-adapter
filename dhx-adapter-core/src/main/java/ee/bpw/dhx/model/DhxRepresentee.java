package ee.bpw.dhx.model;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.util.ConversionUtil;

import eu.x_road.dhx.producer.Representee;

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

  private String memberCode;
  private Date startDate;
  private Date endDate;

  private String name;
  private String system;

  /**
   * Create Representee from member(which is representationList service output).
   * 
   * @param representee - member from which to create representee
   */
  public DhxRepresentee(Representee representee) {
    this.memberCode = representee.getMemberCode();
    if (representee.getStartDate() != null) {
      this.startDate = ConversionUtil.toDate(representee.getStartDate());
    }
    if (representee.getEndDate() != null) {
      this.endDate = ConversionUtil.toDate(representee.getEndDate());
    }
    if (representee.getRepresenteeName() != null) {
      this.name = representee.getRepresenteeName();
    }
    if (representee.getRepresenteeSystem() != null) {
      this.system = representee.getRepresenteeSystem();
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
    this.memberCode = memberCode;
    this.startDate = startDate;
    this.endDate = endDate;
    this.name = name;
    this.system = system;
  }

  @Override
  public String toString() {
    return "memberCode: " + memberCode + " startDate: " + startDate + " endDate: " + endDate
        + "name: " + name + " system: " + system;
  }

  /**
   * Converts representee to member(for representationList service output).
   * 
   * @return - member
   * @throws DhxException - thrown if error occurs while converting
   */
  public Representee convertToRepresentee() throws DhxException {
    Representee representee = new Representee();
    representee.setMemberCode(memberCode);
    representee.setStartDate(ConversionUtil.toGregorianCalendar(this.getStartDate()));
    representee.setEndDate(ConversionUtil.toGregorianCalendar(this.getEndDate()));
    representee.setRepresenteeSystem(system);
    representee.setRepresenteeName(name);
    return representee;
  }


}

package ee.ria.dhx.ws;

import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.config.SoapConfig;

/**
 * Class for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
 * parameter automatically.
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxOrganisationFactory {

  private static String dhxSubsystemPrefix;
  
/**
 * Method for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
 * parameter automatically.
 * @param memberCode - Xroad member code(organisation registration code) of the organisation
 * @param system - system of the organisation(either Xroad subsystem or representee system)
 * @return DhxOrganisation object
 */
  public static DhxOrganisation createDhxOrganisation(String memberCode, String system) {
    if (dhxSubsystemPrefix == null) {
      SoapConfig config = AppContext.getApplicationContext().getBean(SoapConfig.class);
      dhxSubsystemPrefix = config.getDhxSubsystemPrefix();
    }
    return new DhxOrganisation(memberCode, system, dhxSubsystemPrefix);
  }
  
  /**
   * Method for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
   * parameter automatically.
   * @param member - Xroad member of the organisation
   * @return DhxOrganisation object
   */
    public static DhxOrganisation createDhxOrganisation(InternalXroadMember member) {
      if (dhxSubsystemPrefix == null) {
        SoapConfig config = AppContext.getApplicationContext().getBean(SoapConfig.class);
        dhxSubsystemPrefix = config.getDhxSubsystemPrefix();
      }
      return new DhxOrganisation(member, dhxSubsystemPrefix);
    }


}

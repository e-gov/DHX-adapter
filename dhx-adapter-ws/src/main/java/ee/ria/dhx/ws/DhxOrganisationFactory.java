package ee.ria.dhx.ws;

import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocument;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.config.SoapConfig;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
 * parameter automatically.
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxOrganisationFactory {

  @Autowired
  SoapConfig soapConfig;

  /**
   * Method for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
   * parameter automatically.
   * 
   * @param memberCode - Xroad member code(organisation registration code) of the organisation
   * @param system - system of the organisation(either Xroad subsystem or representee system)
   * @return DhxOrganisation object
   */
  public DhxOrganisation createDhxOrganisation(String memberCode, String system) {
    String dhxSubsystemPrefix = soapConfig.getDhxSubsystemPrefix();
    return new DhxOrganisation(memberCode, system, dhxSubsystemPrefix);
  }

  /**
   * Method for creating DhxOrganisation. Simplifies object creation by setting dhxSubsystemPrefix
   * parameter automatically.
   * 
   * @param member - Xroad member of the organisation
   * @return DhxOrganisation object
   */
  public DhxOrganisation createDhxOrganisation(InternalXroadMember member) {
    String dhxSubsystemPrefix = soapConfig.getDhxSubsystemPrefix();
    return new DhxOrganisation(member, dhxSubsystemPrefix);
  }

  /**
   * Method for creating DhxOrganisation. Gets DhxOrganisation from document if representee is
   * present in request, otherwise DhxOrganisation is created from InternalXroadMember.
   * 
   * @param document - sendDocument services request
   * @param service - recipient of the request
   * @return DhxOrganisation object
   */
  public DhxOrganisation createIncomingRecipientOrgnisation(SendDocument document,
      InternalXroadMember service) {
    String code;
    String system;
    if (!StringUtil.isNullOrEmpty(document.getRecipient())) {
      code = document.getRecipient();
      system = document.getRecipientSystem();
    } else {
      code = service.getMemberCode();
      system = service.getSubsystemCode();
    }
    DhxOrganisation recipient = createDhxOrganisation(code, system);
    return recipient;
  }

}

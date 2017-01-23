package ee.ria.dhx.server.persistence.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.entity.StatusHistory;
import ee.ria.dhx.server.persistence.repository.DocumentRepository;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class to perform actions on persistence objects, e.g. create persistence object from DHX specific
 * object.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Service
@Transactional
public class PersistenceService {

  @Autowired
  @Setter
  AddressService addressService;

  @Autowired
  @Setter
  OrganisationRepository organisationRepository;

  @Value("${dhx.server.special-orgnisations}")
  @Setter
  String specialOrganisations;

  @Autowired
  @Setter
  SoapConfig config;

  @Autowired
  @Setter
  FolderRepository folderRepository;

  @Autowired
  @Setter
  DocumentRepository documentRepository;

  private static final String DEFAULT_FOLDERNAME = "/";

  /**
   * Finds persisted organisation using organisation code from capsule. Only active DHX
   * organisations will be returned, otherwise {@link DhxException} will be thrown
   * 
   * @param capsuleOrganisationId organisation id from capsule
   * @return found {@link Organisation}
   * @throws DhxException - if orgnisation is not found or organisation is inactive or not DHX
   *         organisation
   */
  @Loggable
  public Organisation findOrg(String capsuleOrganisationId) throws DhxException {
    Organisation org = null;
    log.debug("Searching member by organisationId:" + capsuleOrganisationId);
    InternalXroadMember member = null;
    if (isSpecialOrganisation(capsuleOrganisationId)) {
      log.debug(
          "Special organisation. Searching organisaiton by subsystem: " + capsuleOrganisationId);
      org = organisationRepository.findBySubSystem(capsuleOrganisationId);
      // if not found, then try using prefix
      if (org == null) {
        org = organisationRepository
            .findBySubSystem(config.getDhxSubsystemPrefix() + "." + capsuleOrganisationId);
      }
    } else {
      // find organisation
      org = organisationRepository.findByRegistrationCodeAndSubSystem(capsuleOrganisationId,
          config.getDhxSubsystemPrefix());
      if (org == null) {
        // find representee
        org = organisationRepository.findByRegistrationCodeAndSubSystem(capsuleOrganisationId,
            null);
      }
      // if member not found, then try to find by registration code and
      // subsystem, by splitting
      // adressee from container
      if (org == null) {
        Integer index = capsuleOrganisationId.lastIndexOf(".");
        if (index > 0) {
          String code = capsuleOrganisationId.substring(index + 1);
          String system = capsuleOrganisationId.substring(0, index);
          log.debug("Searching member by code:" + code + " and subsystem: " + system);
          member = addressService.getClientForMemberCode(code, system);
        }
        if (member == null) {
          throw new DhxException(DhxExceptionEnum.DATA_ERROR,
              "Unable to find member in addressregistry by regsitration code: "
                  + capsuleOrganisationId);
        }
        DhxOrganisation dhxOrganisation = DhxOrganisationFactory.createDhxOrganisation(member);
        org = organisationRepository.findByRegistrationCodeAndSubSystem(dhxOrganisation.getCode(),
            dhxOrganisation.getSystem());
      }

    }
    if (org == null) {
      throw new DhxException(DhxExceptionEnum.DATA_ERROR,
          "Unable to find organisation using organisation code: " + capsuleOrganisationId);
    }
    // throw error if inactive or not DHXOrganisation and not own representee
    if (org.getIsActive() == null || !org.getIsActive()
        || ((org.getDhxOrganisation() == null || !org.getDhxOrganisation())
            && (org.getOwnRepresentee() == null || org.getOwnRepresentee() == false))) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Found organisation is either inactive or not registered as DHX orghanisation. "
              + "Organisation registration code:"
              + org.getRegistrationCode());
    }
    return org;
  }

  /**
   * Finds folder according to folderName, or by default folder name if folderName in input is NULL.
   * If Folder is not found in database, new folder will be created.
   * 
   * @param folderName - name of the folder to find
   * @return - Folder object found
   */
  @Loggable
  public Folder getFolderByNameOrDefaultFolder(String folderName) {
    if (StringUtil.isNullOrEmpty(folderName)) {
      folderName = DEFAULT_FOLDERNAME;
    }
    Folder folder = folderRepository.findByName(folderName);
    if (folder == null) {
      folder = new Folder();
      folder.setName(folderName);
      folderRepository.save(folder);
    }
    return folder;
  }


  /**
   * Method finds or creates new Organisation object according to data from InternalXroadMember. If
   * object was not found in database, if new object is created then it will be saved to database.
   * 
   * @param member {@link InternalXroadMember} to find Organisation for
   * @param representorOnly whether to search only representor
   * @param dhxOrganisation whether to search only dhxOrganisations
   * @return created or found {@link Organisation}
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Organisation getOrganisationFromInternalXroadMemberAndSave(InternalXroadMember member,
      Boolean representorOnly, Boolean dhxOrganisation) throws DhxException {
    Organisation org = getOrganisationFromInternalXroadMember(member, representorOnly);
    if (org.getOrganisationId() == null) {
      org.setDhxOrganisation(dhxOrganisation);
      organisationRepository.save(org);
    }
    return org;
  }

  /**
   * Method finds or creates new Organisation object according to data from InternalXroadMember. If
   * object was not found in database, new object is created but not saved to database.
   * 
   * @param member {@link InternalXroadMember} to find Organisation for
   * @return created or found {@link Organisation}
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member)
      throws DhxException {
    return getOrganisationFromInternalXroadMember(member, false);
  }

  /**
   * Method finds or creates new Organisation object according to data from InternalXroadMember. If
   * object was not found in database, new object is created but not saved to database.
   * 
   * @param member {@link InternalXroadMember} to find Organisation for
   * @param representorOnly whether to search only representor
   * @return created or found {@link Organisation}
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member,
      Boolean representorOnly)
      throws DhxException {
    Boolean newMember = false;
    Organisation organisation =
        organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
            member.getSubsystemCode());
    if (organisation == null) {
      log.debug("Organisation is not found, creating new one.");
      newMember = true;
      organisation = new Organisation();
    }
    // update representors data only if needed
    if (member.getRepresentee() == null || representorOnly) {
      organisation.setIsActive(true);
      organisation.setMemberClass(member.getMemberClass());
      organisation.setName(member.getName());
      organisation.setRegistrationCode(member.getMemberCode());
      organisation.setSubSystem(member.getSubsystemCode());
      organisation.setXroadInstance(member.getXroadInstance());
      organisation.setDhxOrganisation(true);
      organisation.setRepresentor(null);
      organisation.setRepresenteeStart(null);
      organisation.setRepresenteeEnd(null);
    }
    if (member.getRepresentee() != null && !representorOnly) {
      log.debug("Organisation is representee.");
      if (newMember) {
        // we cannot create new representor with representee. first
        // insert representor without representee, then representee
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Trying to insert representee, but representor is not in database! representor:"
                + member.getMemberCode() + "/" + member.getSubsystemCode() + " representee: "
                + member.getRepresentee().getRepresenteeCode() + "/"
                + member.getRepresentee().getRepresenteeSystem());
      }
      Organisation representeeOrganisation =
          organisationRepository.findByRegistrationCodeAndSubSystem(
              member.getRepresentee().getRepresenteeCode(),
              member.getRepresentee().getRepresenteeSystem());
      if (representeeOrganisation == null) {
        log.debug("Representee organisation is not found, creating new one.");
        representeeOrganisation = new Organisation();
      }
      representeeOrganisation.setIsActive(true);
      representeeOrganisation.setName(member.getRepresentee().getRepresenteeName());
      representeeOrganisation.setRegistrationCode(member.getRepresentee().getRepresenteeCode());
      representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
      if (member.getRepresentee().getStartDate() != null) {
        representeeOrganisation
            .setRepresenteeStart(new Timestamp(member.getRepresentee().getStartDate().getTime()));
      } else if (representeeOrganisation.getRepresenteeStart() != null) {
        representeeOrganisation.setRepresenteeStart(new Timestamp(new Date().getTime()));
      }
      if (member.getRepresentee().getEndDate() != null) {
        representeeOrganisation
            .setRepresenteeEnd(new Timestamp(member.getRepresentee().getEndDate().getTime()));
      }
      representeeOrganisation.setRepresentor(organisation);
      representeeOrganisation.setDhxOrganisation(true);
      representeeOrganisation.setMemberClass(null);
      representeeOrganisation.setXroadInstance(null);
      // organisation.addRepresentee(representeeOrganisation);
      organisation = representeeOrganisation;
    }
    return organisation;
  }

  /**
   * Method defines if organisation is one of the special organisations that are in the capsule
   * without registration code, but with system name.
   * 
   * @param organisationCode organisation code to check
   * @return whether organistion code is special or not
   */
  @Loggable
  public Boolean isSpecialOrganisation(String organisationCode) {
    String specialOrgs = "," + getSpecialOrganisations() + ",";
    log.debug("specialOrgs: " + specialOrgs + "  organisationCode:" + organisationCode);
    if (specialOrgs.indexOf("," + organisationCode + ",") >= 0) {
      return true;
    }
    return false;
  }

  /**
   * Returns the specialOrganisations.
   * 
   * @return the specialOrganisations
   */
  public String getSpecialOrganisations() {
    return specialOrganisations;
  }


  /**
   * Checks is representee is valid(by start and end date of the representee).
   * 
   * @param member representee member to check
   * @return whther representee is valid
   * @throws DhxException exception thrown if representee is null or start date is null
   */
  public Boolean isRepresenteeValid(InternalXroadMember member) throws DhxException {
    Long curDate = new Date().getTime();
    if (member.getRepresentee() == null || member.getRepresentee().getStartDate() == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Something went wrong! Start date of representee is empty "
              + "or organisation is not representee!");
    }
    if (member.getRepresentee().getStartDate().getTime() <= curDate
        && (member.getRepresentee().getEndDate() == null
            || member.getRepresentee().getEndDate().getTime() >= curDate)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Sometimes DHX addressee and DVK addresse might be different. In DHX there must be always
   * registration code, in DVK there might be system also.
   * 
   * @param memberCode memberCode to use to transform to DVK capsule addressee
   * @param subsystem subsystem to use to transform to DVK capsule addressee
   * @return capsule addressee accordinbg to DVK
   */
  @Loggable
  public String toDvkCapsuleAddressee(String memberCode, String subsystem) {
    String dvkCode = null;
    if (!StringUtil.isNullOrEmpty(subsystem)
        && subsystem.startsWith(config.getDhxSubsystemPrefix() + ".")) {
      String system = subsystem.substring(config.getDhxSubsystemPrefix().length() + 1);
      // String perfix = subsystem.substring(0, index);
      log.debug("found system with subsystem: " + system);
      if (isSpecialOrganisation(system)) {
        dvkCode = system;
      } else {
        dvkCode = system + "." + memberCode;
      }

    } else if (!StringUtil.isNullOrEmpty(subsystem)
        && !subsystem.equals(config.getDhxSubsystemPrefix())) {
      if (isSpecialOrganisation(subsystem)) {
        dvkCode = subsystem;
      } else {
        dvkCode = subsystem + "." + memberCode;
      }
    } else {

      dvkCode = memberCode;
    }
    return dvkCode;
  }

  /**
   * Method creates and add new status history according to recipient's data.
   * 
   * @param recipient recipient to create status history for
   */
  public void addStatusHistory(Recipient recipient) {
    StatusHistory history = new StatusHistory();
    history.setRecipientStatusId(recipient.getRecipientStatusId());
    history.setRecipient(recipient);
    history.setStatusChangeDate(recipient.getStatusChangeDate());
    history.setMetaxml(recipient.getMetaxml());
    history.setStatusId(recipient.getStatusId());
    history.setFaultString(recipient.getFaultString());
    history.setFaultDetail(recipient.getFaultDetail());
    history.setFaultCode(recipient.getFaultCode());
    history.setFaultActor(recipient.getFaultActor());
    recipient.addStatusHistory(history);
  }

  /**
   * Method returns list of all addressees.
   * 
   * @return list of all addressees
   */
  @Loggable(Loggable.DEBUG)
  public List<Organisation> getAdresseeList() {
    List<Organisation> result = new ArrayList<Organisation>();
    List<Organisation> orgs =
        organisationRepository.findByIsActiveAndDhxOrganisation(true, true);
    if (orgs != null && orgs.size() > 0) {
      result.addAll(orgs);
    }
    List<Organisation> representeeOrgs =
        organisationRepository.findByIsActiveAndOwnRepresentee(true, true);
    if (representeeOrgs != null && representeeOrgs.size() > 0) {
      for (Organisation representee : representeeOrgs) {
        if (!result.contains(representee)) {
          result.add(representee);
        }
      }
    }

    return result;
  }


}

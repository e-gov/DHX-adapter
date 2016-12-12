package ee.ria.dhx.server.persistence.service;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.service.AddressService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Class to perform actions on persistence objects, e.g. create persistence object from DHX specific
 * object.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Service
public class PersistenceService {



  @Autowired
  AddressService addressService;

  @Autowired
  OrganisationRepository organisationRepository;


  @Autowired
  FolderRepository folderRepository;

  private final String DEFAULT_FOLDERNAME = "/";


  /**
   * Finds persisted organisation using organisation id from capsule.
   * @param containerOrganisationId
   * @return
   * @throws DhxException
   */
  public Organisation findOrg(String containerOrganisationId) throws DhxException {
    Organisation org = null;
    log.debug("Searching member by organisationId:" + containerOrganisationId);
    InternalXroadMember member = null;
    try {
      member = addressService.getClientForMemberCode(containerOrganisationId, null);
    } catch (DhxException ex) {
      log.debug(
          "Erro occured while searching org. ignoring error and continue!" + ex.getMessage(), ex);
    }
    // if member not found, then try to find by registration code and subsystem, by splitting
    // adressee from container
    if (member == null) {
      Integer index = containerOrganisationId.lastIndexOf(".");
      if (index > 0) {
        String code = containerOrganisationId.substring(index);
        String system = containerOrganisationId.substring(0, index);
        log.debug("Searching member by code:" + code + " and subsystem: " + system);
        member = addressService.getClientForMemberCode(code, system);
      }


      if (member == null) {
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Unable to find member in addressregistry by regsitration code: "
                + containerOrganisationId);
      }

    }
    org = organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
        member.getSubsystemCode());
    if (org == null) {
      throw new DhxException(DhxExceptionEnum.DATA_ERROR,
          "Unable to find organisation using member: "
              + member.toString());
    }
    return org;
  }

  /**
   * Finds folder according to folderName, or by default folder name if folderName in input is NULL.
   * 
   * @param folderName - name of the folder to find
   * @return - Folder object found
   */
  public Folder getFolderByNameOrDefaultFolder(String folderName) {
    if (folderName == null) {
      folderName = DEFAULT_FOLDERNAME;
    }
    Folder folder = folderRepository.findByName(folderName);
    return folder;
  }

  /**
   * Method finds or creates new Organisation object according to data from InternalXroadMember. If
   * object was not found in database, new object is created but not saved to database.
   * 
   * @param member - InternalXroadMember to find Organisation for
   * @return - created or found Organisation
   * @throws DhxException
   */
  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member)
      throws DhxException {
    return getOrganisationFromInternalXroadMember(member, false);
  }

  public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member,
      Boolean representorOnly)
      throws DhxException {
    Boolean newMember = false;
    Organisation organisation =
        organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
            member.getSubsystemCode());
    if (organisation == null) {
      newMember = true;
      organisation = new Organisation();
    }
    organisation.setIsActive(true);
    organisation.setMemberClass(member.getMemberClass());
    organisation.setName(member.getName());
    organisation.setRegistrationCode(member.getMemberCode());
    organisation.setSubSystem(member.getSubsystemCode());
    organisation.setXroadInstance(member.getXroadInstance());
    organisation.setDhxOrganisation(true);
    if (member.getRepresentee() != null && !representorOnly) {
      if (newMember) {
        // we cannot insert new representor with representee
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Trying to insert representee, but representor is not in database! ");
      }
      Organisation representeeOrganisation =
          organisationRepository.findByRegistrationCodeAndSubSystem(member.getRepresentee()
              .getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem());
      if (representeeOrganisation == null) {
        representeeOrganisation = new Organisation();
      }
      representeeOrganisation.setIsActive(true);
      representeeOrganisation.setName(member.getRepresentee().getRepresenteeName());
      representeeOrganisation.setRegistrationCode(member.getRepresentee().getRepresenteeCode());
      representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
      if (member.getRepresentee().getStartDate() != null) {
        representeeOrganisation.setRepresenteeStart(new Timestamp(member.getRepresentee()
            .getStartDate().getTime()));
      }
      if (member.getRepresentee().getEndDate() != null) {
        representeeOrganisation.setRepresenteeEnd(new Timestamp(member.getRepresentee()
            .getEndDate().getTime()));
      }
      representeeOrganisation.setRepresentor(organisation);
      representeeOrganisation.setDhxOrganisation(true);
      // organisation.addRepresentee(representeeOrganisation);
      organisation = representeeOrganisation;
    }
    return organisation;
  }


}

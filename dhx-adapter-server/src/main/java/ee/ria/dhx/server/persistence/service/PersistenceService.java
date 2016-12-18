package ee.ria.dhx.server.persistence.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Class to perform actions on persistence objects, e.g. create persistence
 * object from DHX specific object.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Service
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

	private final String DEFAULT_FOLDERNAME = "/";

	/**
	 * Finds persisted organisation using organisation code from capsule.
	 * 
	 * @param containerOrganisationId
	 * @return
	 * @throws DhxException
	 */
	@Loggable
	public Organisation findOrg(String containerOrganisationId) throws DhxException {
		Organisation org = null;
		log.debug("Searching member by organisationId:" + containerOrganisationId);
		InternalXroadMember member = null;
		if (isSpecialOrganisation(containerOrganisationId)) {
			log.debug("Special organisation. Searching organisaiton by subsystem: " + containerOrganisationId);
			org = organisationRepository.findBySubSystem(containerOrganisationId);
		} else {
			//find organisation
			org = organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId,
					config.getDhxSubsystemPrefix());
			if(org == null) {
				//find representee
				org = organisationRepository.findByRegistrationCodeAndSubSystem(containerOrganisationId, null);
			}
			// if member not found, then try to find by registration code and
			// subsystem, by splitting
			// adressee from container
			if (org == null) {
				Integer index = containerOrganisationId.lastIndexOf(".");
				if (index > 0) {
					String code = containerOrganisationId.substring(index + 1);
					String system = containerOrganisationId.substring(0, index);
					log.debug("Searching member by code:" + code + " and subsystem: " + system);
					member = addressService.getClientForMemberCode(code, system);
				}
				if (member == null) {
					throw new DhxException(DhxExceptionEnum.DATA_ERROR,
							"Unable to find member in addressregistry by regsitration code: "
									+ containerOrganisationId);
				}
				org = organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
						member.getSubsystemCode());
			}

		}
		if (org == null) {
			throw new DhxException(DhxExceptionEnum.DATA_ERROR,
					"Unable to find organisation using organisation code: " + containerOrganisationId);
		}
		return org;
	}

	/**
	 * Finds folder according to folderName, or by default folder name if
	 * folderName in input is NULL.
	 * 
	 * @param folderName
	 *            - name of the folder to find
	 * @return - Folder object found
	 */
	@Loggable
	public Folder getFolderByNameOrDefaultFolder(String folderName) {
		if (folderName == null) {
			folderName = DEFAULT_FOLDERNAME;
		}
		Folder folder = folderRepository.findByName(folderName);
		return folder;
	}

	/**
	 * Method finds or creates new Organisation object according to data from
	 * InternalXroadMember. If object was not found in database, new object is
	 * created but not saved to database.
	 * 
	 * @param member
	 *            - InternalXroadMember to find Organisation for
	 * @return - created or found Organisation
	 * @throws DhxException
	 */
	@Loggable
	public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member) throws DhxException {
		return getOrganisationFromInternalXroadMember(member, false);
	}

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

	@Loggable
	public Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member, Boolean representorOnly)
			throws DhxException {
		Boolean newMember = false;
		Organisation organisation = organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
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
		organisation.setRepresentor(null);
		organisation.setRepresenteeStart(null);
		organisation.setRepresenteeEnd(null);
		if (member.getRepresentee() != null && !representorOnly) {
			if (newMember) {
				// we cannot create new representor with representee. first
				// insert representor without representee, then representee
				throw new DhxException(DhxExceptionEnum.DATA_ERROR,
						"Trying to insert representee, but representor is not in database! ");
			}
			Organisation representeeOrganisation = organisationRepository.findByRegistrationCodeAndSubSystem(
					member.getRepresentee().getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem());
			if (representeeOrganisation == null) {
				representeeOrganisation = new Organisation();
			}
			representeeOrganisation.setIsActive(true);
			representeeOrganisation.setName(member.getRepresentee().getRepresenteeName());
			representeeOrganisation.setRegistrationCode(member.getRepresentee().getRepresenteeCode());
			representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
			if (member.getRepresentee().getStartDate() != null) {
				representeeOrganisation
						.setRepresenteeStart(new Timestamp(member.getRepresentee().getStartDate().getTime()));
			} else {
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
	 * Method defines if organisation is one of the special organisations that
	 * are in the capsule without registration code, but with system name.
	 * 
	 * @param organisationCode
	 * @return
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
	 * @return the specialOrganisations
	 */
	public String getSpecialOrganisations() {
		return specialOrganisations;
	}
 
	private Boolean isRepresenteeValid(Organisation org) throws DhxException {
		Long curDate = new Date().getTime();
		if (org.getRepresenteeStart() == null) {
			throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
					"Something went wrong! Start date of representee is empty or organisation is not representee!");
		}
		if (org.getRepresenteeStart().getTime() <= curDate
				&& (org.getRepresenteeEnd() == null || org.getRepresenteeEnd().getTime() >= curDate)) {
			return true;
		} else {
			return false;
		}
	}
	
	public Boolean isRepresenteeValid(InternalXroadMember member) throws DhxException {
		Long curDate = new Date().getTime();
		if (member.getRepresentee() == null || member.getRepresentee().getStartDate() == null) {
			throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
					"Something went wrong! Start date of representee is empty or organisation is not representee!");
		}
		if (member.getRepresentee().getStartDate().getTime() <= curDate
				&& (member.getRepresentee().getEndDate() == null || member.getRepresentee().getEndDate().getTime() >= curDate)) {
			return true;
		} else {
			return false;
		}
	}

}

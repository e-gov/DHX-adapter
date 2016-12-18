package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Organisation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganisationRepository extends CrudRepository<Organisation, Long> {

	Organisation findByRegistrationCodeAndSubSystem(String registrationCode, String subsystem);
	
	Organisation findByRegistrationCodeAndSubSystemAndRepresentor(String registrationCode, String subsystem, Organisation representor);

	Organisation findBySubSystem(String subsystem);

	List<Organisation> findByIsActiveAndDhxOrganisation(Boolean isActive, Boolean dhxOrganisation);

	List<Organisation> findByIsActiveAndOwnRepresentee(Boolean isActive, Boolean OwnRepresentee);
}

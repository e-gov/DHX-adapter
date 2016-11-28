package ee.ria.dhx.server.repository;


import ee.ria.dhx.server.entity.Organisation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganisationRepository extends CrudRepository<Organisation, Long> {

  Organisation findByRegistrationCodeAndSubSystem(String registrationCode, String subsystem);
  
  List<Organisation> findByIsActive(Boolean isActive);
}

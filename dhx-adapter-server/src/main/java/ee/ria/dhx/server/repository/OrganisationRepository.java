package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Organisation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganisationRepository extends CrudRepository<Organisation, Long> {

  List<Organisation> findByMemberCodeAndSubsystem(String memberCode, String subsystem);
}

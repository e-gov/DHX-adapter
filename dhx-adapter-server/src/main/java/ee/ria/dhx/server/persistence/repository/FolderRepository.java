package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Folder;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository class for CRUD actions with Folder table.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface FolderRepository extends CrudRepository<Folder, Long> {

  public Folder findByName(String name);

}

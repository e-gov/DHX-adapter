package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Folder;

import org.springframework.data.repository.CrudRepository;

public interface FolderRepository extends CrudRepository<Folder, Long> {
  
  public Folder findByName(String name);

}

package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Folder;

import org.springframework.data.repository.CrudRepository;

public interface ClassificatorRepository  extends CrudRepository<Classificator, Long> {
  
  public Classificator findByName(String name);

}
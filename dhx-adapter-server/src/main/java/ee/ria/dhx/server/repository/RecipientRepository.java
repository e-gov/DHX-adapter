package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Recipient;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecipientRepository extends CrudRepository<Recipient, Long> {
  
  public List<Recipient> findByStatus(Classificator status);
  
  public List<Recipient> findByStatusAndDhxInternalConsignmentIdNull(Classificator status);

}
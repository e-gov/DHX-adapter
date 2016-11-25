package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Document;
import ee.ria.dhx.server.entity.Folder;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, Long> {
  


}
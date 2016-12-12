package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, Long> {

  List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
      Organisation org,
      Integer statusId, Folder folder);

  List<Document> findByDocumentIdIn(List<Integer> ids);

  Document findByDocumentId(Integer id);
}

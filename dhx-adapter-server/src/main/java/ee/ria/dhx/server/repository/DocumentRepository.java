package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Document;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, Long> {

  List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
      Organisation org,
      Integer statusId, Folder folder);

  List<Document> findByDocumentIdIn(List<Integer> ids);

  Document findByDocumentId(Integer id);
}

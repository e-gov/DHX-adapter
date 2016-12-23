package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

/**
 * Repository class for CRUD actions with Document table.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface DocumentRepository extends CrudRepository<Document, Long> {

  List<Document> findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(
      Boolean outgoingDocument, Organisation org,
      Integer statusId, Folder folder, Pageable pageable);

  List<Document> findByOutgoingDocumentAndTransportsRecipientsOrganisationAndTransportsRecipientsStatusId(
      Boolean outgoingDocument, Organisation org,
      Integer statusId, Pageable pageable);

  List<Document> findByDocumentIdIn(List<Long> ids);
  
  List<Document> findByDateCreatedLessThanAndTransportsStatusId (Date dateCreated, Integer statusId);

  Document findByDocumentId(Long id);
}

package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentRepository extends CrudRepository<Document, Long> {

	List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolder(Organisation org,
			Integer statusId, Folder folder, Pageable pageable);

	List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusId(Organisation org,
			Integer statusId, Pageable pageable);

	List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndTransportsRecipientsStructuralUnit(
			Organisation org, Integer statusId, String structuralUnit, Pageable pageable);

	List<Document> findByTransportsRecipientsOrganisationAndTransportsRecipientsStatusIdAndFolderAndTransportsRecipientsStructuralUnit(
			Organisation org, Integer statusId, Folder folder, String structuralUnit, Pageable pageable);

	List<Document> findByDocumentIdIn(List<Long> ids);

	Document findByDocumentId(Long id);
}

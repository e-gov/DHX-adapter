package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Repository class for CRUD actions with Recipient table.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface RecipientRepository extends CrudRepository<Recipient, Long> {

  public List<Recipient> findByStatusId(Integer statusId);

  public List<Recipient> findByStatusIdAndTransportDokumentOutgoingDocumentAndDhxInternalConsignmentIdNull(
      Integer statusId, Boolean outgoing);

  public List<Recipient> findByTransport_SendersOrganisationAndDhxExternalConsignmentId(
      Organisation org, String dhxExternalConsignmentId);

  public Recipient findByTransportDokumentDocumentIdAndOrganisation(Integer documentId,
      Organisation org);

  public Recipient findByTransportDokumentDocumentIdAndTransportDokumentFolderAndOrganisation(
      Integer documentId, Folder folder, Organisation org);

}

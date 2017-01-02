package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;

/**
 * Repository class for CRUD actions with Recipient table.
 * 
 * @author Aleksei Kokarev
 *
 */
public interface RecipientRepository extends JpaRepository<Recipient, Long> {

  public List<Recipient> findByStatusId(Integer statusId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  public List<Recipient> findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNull(
      Integer statusId, Boolean outgoing);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  public List<Recipient> findByStatusIdAndOutgoingAndDhxInternalConsignmentIdNotNullAndDateModifiedLessThan(
      Integer statusId, Boolean outgoing, Date dateCreated);

  public List<Recipient> findByTransport_SendersOrganisationAndDhxExternalConsignmentId(
      Organisation org, String dhxExternalConsignmentId);

  public Recipient findByTransportDokumentDocumentIdAndOrganisation(Integer documentId,
      Organisation org);

  public Recipient findByTransportDokumentDocumentIdAndTransportDokumentFolderAndOrganisation(
      Integer documentId, Folder folder, Organisation org);
  
  /*@Lock(LockModeType.PESSIMISTIC_WRITE)
  public List<Recipient> findByRecipientIdIn(List<Long> ids);
  
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  public Recipient findByRecipientId(Long id);*/

}

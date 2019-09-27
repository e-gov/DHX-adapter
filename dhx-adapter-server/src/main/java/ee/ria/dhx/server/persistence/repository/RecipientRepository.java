package ee.ria.dhx.server.persistence.repository;

import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;

import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.LockModeType;

/**
 * Repository class for CRUD actions with Recipient table.
 * 
 * @author Aleksei Kokarev
 *
 */
@Transactional
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

  @Transactional
  public Recipient findByRecipientIdAndTransportDokumentDocumentId(Long recipientId, Long documentId);

  @Transactional
  @Query("SELECT r FROM ee.ria.dhx.server.persistence.entity.Recipient r " +
         "JOIN r.transport t " +
         "LEFT OUTER JOIN t.senders s " +
         "LEFT OUTER JOIN s.organisation so " +
         "JOIN t.dokument d " +
         "WHERE " +
               "r.outgoing = true " +
               "AND (true = :#{#statuses == null || #statuses.size() == 0} OR r.statusId IN (:#{#statuses.![classificatorId]})) " +
               "AND (true = :#{#isOutgoing == null} OR d.outgoingDocument = :#{#isOutgoing}) " +
               "AND (true = :#{T(org.apache.commons.lang3.StringUtils).isEmpty(#senderRegCode)} OR so.registrationCode LIKE %:#{#senderRegCode}%) " +
               "AND (true = :#{T(org.apache.commons.lang3.StringUtils).isEmpty(#senderSubsystem)} OR so.subSystem LIKE %:#{#senderSubsystem}%) " +
               //"AND (true = :#{T(org.apache.commons.lang3.StringUtils).isEmpty(#documentTitle)} OR d.title LIKE %:documentTitle%) " + // TODO: Uncomment if document title has been introduced
               "AND (true = :#{#dateCreatedFrom == null} OR d.dateCreated >= :#{#dateCreatedFrom}) " +
               "AND (true = :#{#dateCreatedTo == null} OR d.dateCreated <= :#{#dateCreatedTo != null ? T(org.apache.commons.lang3.time.DateUtils).addDays(#dateCreatedTo, 1) : null}) " +
         "ORDER BY d.dateCreated DESC")
  Page<Recipient> findAllBy(
          @Param("statuses") Collection<StatusEnum> statuses,
          @Param("isOutgoing") Boolean isOutgoing,
          @Param("senderRegCode") String senderRegCode,
          @Param("senderSubsystem") String senderSubsystem,
          //@Param("documentTitle") String documentTitle, // TODO: Uncomment if document title has been introduced
          @Param("dateCreatedFrom") Date dateCreatedFrom,
          @Param("dateCreatedTo") Date dateCreatedTo,
          Pageable pageable);

  /*
   * @Lock(LockModeType.PESSIMISTIC_WRITE) public List<Recipient> findByRecipientIdIn(List<Long>
   * ids);
   * 
   * @Lock(LockModeType.PESSIMISTIC_WRITE) public Recipient findByRecipientId(Long id);
   */

}

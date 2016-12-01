package ee.ria.dhx.server.repository;

import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.entity.Recipient;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecipientRepository extends CrudRepository<Recipient, Long> {
  
  public List<Recipient> findByStatus(Classificator status);
  
  public List<Recipient> findByStatusAndTransportDokumentOutgoingDocumentAndDhxInternalConsignmentIdNull(Classificator status, Boolean outgoing);
  
  public List<Recipient> findByTransport_SendersOrganisationAndDhxExternalConsignmentId(Organisation org, String dhxExternalConsignmentId);
  
  public Recipient findByTransportDokumentDocumentIdAndOrganisation(Integer documentId, Organisation org);
  
  public Recipient findByTransportDokumentDocumentIdAndTransportDokumentFolderAndOrganisation(Integer documentId, Folder folder, Organisation org);

}
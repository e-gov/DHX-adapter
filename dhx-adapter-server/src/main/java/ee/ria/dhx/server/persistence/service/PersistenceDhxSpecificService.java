package ee.ria.dhx.server.persistence.service;


import com.jcabi.aspects.Loggable;









import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.persistence.entity.Document;
import ee.ria.dhx.server.persistence.entity.Organisation;
import ee.ria.dhx.server.persistence.entity.Recipient;
import ee.ria.dhx.server.persistence.enumeration.StatusEnum;
import ee.ria.dhx.server.persistence.repository.DocumentRepository;
import ee.ria.dhx.server.persistence.repository.OrganisationRepository;
import ee.ria.dhx.server.persistence.repository.RecipientRepository;
import ee.ria.dhx.server.service.ConvertationService;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.ws.DhxOrganisationFactory;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class PersistenceDhxSpecificService implements DhxImplementationSpecificService {

  private List<InternalXroadMember> members;

  @Autowired
  OrganisationRepository organisationRepository;

  @Autowired
  RecipientRepository recipientRepository;


  @Autowired
  ConvertationService convertationService;

  @Autowired
  DocumentRepository documentRepository;
  
  @Autowired
  CapsuleService capsuleService;
  
  @Autowired
  PersistenceService persistenceService;
  
  @Override
  public List<DhxRepresentee> getRepresentationList() throws DhxException {
    List<DhxRepresentee> representees = new ArrayList<DhxRepresentee>();
      List<Organisation> orgs = organisationRepository.findByIsActiveAndOwnRepresentee(true, true);
      for (Organisation org : orgs) {
        representees.add(getRepresenteeFromOrganisation(org));
      }
      return representees;
    }



  @Override
  public boolean isDuplicatePackage(InternalXroadMember from, String consignmentId)
  {
    DhxOrganisation dhxOrg = DhxOrganisationFactory.createDhxOrganisation(from);
    Organisation org =
        organisationRepository.findByRegistrationCodeAndSubSystem(dhxOrg.getCode(),
            dhxOrg.getSystem());
    List<Recipient> recipients =
        recipientRepository.findByTransport_SendersOrganisationAndDhxExternalConsignmentId(org,
            consignmentId);
    if (recipients != null && recipients.size() > 0) {
      return true;
    }
    return false;
  }



  @Override
  @Loggable
  public String receiveDocument(IncomingDhxPackage document,
      MessageContext context) throws DhxException {
    log.debug(
        "String receiveDocument(DhxDocument document) externalConsignmentId: {}",
        document.getExternalConsignmentId());
    Document doc = capsuleService.getDocumentFromIncomingContainer(document, document.getParsedContainerVersion());
    documentRepository.save(doc);
    // by container definition and DHX protocol we know that those arrays are all not null and only
    // have 1 object in it
    return doc.getTransports().get(0).getRecipients().get(0).getRecipientId().toString();
  }


  /*
   * public abstract List<DhxRepresentee> getRepresentationList() throws DhxException;
   */

  public List<InternalXroadMember> getAdresseeList() throws DhxException {
    if (members == null) {
      members = new ArrayList<InternalXroadMember>();
      List<Organisation> orgs = organisationRepository.findByIsActiveAndDhxOrganisation(true, true);
      for (Organisation org : orgs) {
        members.add(getInternalXroadMemberFromOrganisation(org));
      }
    }
    return members;
  }

  private InternalXroadMember getInternalXroadMemberFromOrganisation(Organisation org) {
    Organisation mainOrg = org;
    DhxRepresentee representee = null;
    if (org.getRepresentor() != null) {
      mainOrg = org.getRepresentor();
      representee = getRepresenteeFromOrganisation(org);
          
    }
    InternalXroadMember member =
        new InternalXroadMember(mainOrg.getXroadInstance(), mainOrg.getMemberClass(),
            mainOrg.getRegistrationCode(),
            mainOrg.getSubSystem(), mainOrg.getName(), representee);
    return member;
  }
  
  private DhxRepresentee getRepresenteeFromOrganisation (Organisation org) {
    return new DhxRepresentee(org.getRegistrationCode(), org.getRepresenteeStart(),
      org.getRepresenteeEnd(), org.getName(), org.getSubSystem());
  }



  @Override
  public void saveAddresseeList(List<InternalXroadMember> members)
      throws DhxException {
    updateNotActiveAdressees(members);
    List<Organisation> organisations = new ArrayList<Organisation>();
    for (InternalXroadMember member : members) {
      if (member.getRepresentee() == null) {
        organisations.add(persistenceService.getOrganisationFromInternalXroadMember(member));
      }
    }
    organisationRepository.save(organisations);
    Iterable<Organisation> orgs = organisationRepository.findAll();
    for (Organisation org : orgs) {
      log.debug("org code" + org.getRegistrationCode() + " system: " + org.getSubSystem());
    }
    organisations = new ArrayList<Organisation>();
    for (InternalXroadMember member : members) {
      if (member.getRepresentee() != null) {
        organisations.add(persistenceService.getOrganisationFromInternalXroadMember(member));
      }
    }
    organisationRepository.save(organisations);


    this.members = members;
  }

  private void updateNotActiveAdressees(List<InternalXroadMember> members) {
    // adressees count should not be too big, get ALL
    Iterable<Organisation> allOrgs = organisationRepository.findAll();
    Boolean found = false;
    List<Organisation> changedOrgs = new ArrayList<Organisation>();
    for (Organisation org : allOrgs) {
      found = false;
      for (InternalXroadMember member : members) {
        if (member.getMemberCode().equals(org.getRegistrationCode()) &&
            (member.getSubsystemCode() == null && org.getSubSystem() == null)
            || member.getSubsystemCode().equals(org.getSubSystem())) {
          found = true;
          break;
        }
      }
      if (!found) {
        org.setIsActive(false);
        changedOrgs.add(org);
      }
    }
    organisationRepository.save(changedOrgs);
  }



  @Override
  public void saveSendResult(DhxSendDocumentResult finalResult,
      List<AsyncDhxSendDocumentResult> retryResults) {
    log.info("saveSendResult invoked.");
    try {
      String recipientIdStr = finalResult.getSentPackage().getInternalConsignmentId();
      Integer recipientId = Integer.parseInt(recipientIdStr);
      Recipient recipient = recipientRepository.findOne(new Long(recipientId));
      SendDocumentResponse docResponse = finalResult.getResponse();
      recipient.setSendingEnd(new Timestamp((new Date()).getTime()));
      Integer successStatusId = StatusEnum.RECEIVED.getClassificatorId();
      if (docResponse.getFault() == null) {
        log.debug("Document was succesfuly sent to DHX");
        recipient.setDhxExternalReceiptId(docResponse
            .getReceiptId());
        // recipient.setRecipientStatusId(11);
        recipient.setStatusId(successStatusId);
      } else {
        log.debug("Fault occured while sending document to DHX");
        log.debug("All attempts to send documents were done. Saving document as failed.");
        Integer failedStatusId = StatusEnum.FAILED.getClassificatorId();
        // recipient.setRecipientStatusId(5);
        recipient.setStatusId(failedStatusId);
        // recipient.setSendingEndDate(new Date());
        String faultString = "";
        if (retryResults != null && retryResults.size() > 0) {
          faultString = faultString + " Total retries count: " + retryResults.size()
              + " Results for individual retries: ";
          for (AsyncDhxSendDocumentResult result : retryResults) {
            faultString = faultString + "\n Retry date: " + result.getTryDate()
                + " Technical error:";
            if (result.getResult().getOccuredException() != null) {
              faultString =
                  faultString + " Error message:"
                      + result.getResult().getOccuredException().getMessage()
                      + " Stacktrace: "
                      + ExceptionUtils.getStackTrace(result.getResult().getOccuredException());
            }
          }
        }
        faultString = docResponse.getFault()
            .getFaultString() + faultString;
        recipient.setFaultCode(docResponse.getFault()
            .getFaultCode());
        recipient.setFaultString(docResponse.getFault().getFaultString());
        recipient.setFaultDetail(faultString.substring(0, (faultString.length() > 1900
            ? 1900
            : faultString.length())));
      }
      boolean allSent = true;
      for (Recipient docRecipient : recipient.getTransport().getRecipients()) {
        if (!docRecipient.getStatusId().equals(StatusEnum.RECEIVED.getClassificatorId())) {
          allSent = false;
          break;
        }
      }
      if (allSent
          && !recipient.getTransport().getStatusId()
              .equals(StatusEnum.RECEIVED.getClassificatorId())) {
        recipient.getTransport().setStatusId(successStatusId);
        documentRepository.save(recipient.getTransport().getDokument());
      }
      recipientRepository.save(recipient);
    } catch (Exception ex) {
      log.error("Error occured while saving send results. " + ex.getMessage(), ex);
    } finally {}
  }

}

package ee.ria.dhx.server.service;


import com.jcabi.aspects.Loggable;


import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.entity.Recipient;
import ee.ria.dhx.server.repository.ClassificatorRepository;
import ee.ria.dhx.server.repository.OrganisationRepository;
import ee.ria.dhx.server.repository.RecipientRepository;
import ee.ria.dhx.server.service.util.StatusEnum;
import ee.ria.dhx.types.AsyncDhxSendDocumentResult;
import ee.ria.dhx.types.DhxOrganisation;
import ee.ria.dhx.types.DhxRepresentee;
import ee.ria.dhx.types.DhxSendDocumentResult;
import ee.ria.dhx.types.IncomingDhxPackage;
import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.types.eu.x_road.dhx.producer.SendDocumentResponse;
import ee.ria.dhx.util.StringUtil;
import ee.ria.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;









import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ws.context.MessageContext;

import java.io.File;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;

@Service
@Slf4j
public class DhxAdapterServerSpecificService extends ExampleDhxImplementationSpecificService {

  private List<InternalXroadMember> members;

  @Autowired
  OrganisationRepository organisationRepository;
  
  @Autowired
  RecipientRepository recipientRepository;
  
  
  @Autowired
  ClassificatorRepository classificatorRepository;

  /*
   * public abstract boolean isDuplicatePackage(InternalXroadMember from, String consignmentId)
   * throws DhxException;
   */


  @Override
  @Loggable
  public String receiveDocument(IncomingDhxPackage document,
          MessageContext context) throws DhxException {
      log.debug(
              "String receiveDocument(DhxDocument document) externalConsignmentId: {}",
              document.getExternalConsignmentId());
     
    //  ArrayList<Document> serverDocuments = new ArrayList<Document>();
      Integer recipientId;
      return null;
     /* try {
         // conn = getConnection();
        Organisation recipientOrg = organisationRepository.findByRegistrationCodeAndSubSystem(document.getRecipient().getCode(), document.getRecipient().getSystem());
        DhxOrganisation sender = new DhxOrganisation(document.getClient());
        Organisation senderOrg =  organisationRepository.findByRegistrationCodeAndSubSystem(sender.getCode(), sender.getSystem());
        // recipientAsutus.loadByRegNr(dvkRegCode, conn);
          if (recipientOrg == null) {
              throw new DhxException(DhxExceptionEnum.WRONG_RECIPIENT,
                      "Unable to find recipient oraganisation. Dhx organisation: " + document.getRecipient().toString());
          }
          DataHandler data = document.getDocumentFile();
          String senderTargetFolder = "/";
          if (document.getParsedContainer() !=null) {
            DecContainer container =
                (DecContainer) document.getParsedContainer();
            if(container.getDecMetadata() != null && container.getDecMetadata().getDecFolder() !=null) {
              senderTargetFolder = container.getDecMetadata().getDecFolder();
            }
          }
          /*Document doc = Document
                      .fromXML(
                              docFiles.subFiles.get(i),
                              user.getOrganizationID(),
                              (Settings.Server_ValidateXmlFiles || Settings.Server_ValidateSignatures),
                              conn, xroadHeader);

              SendDocuments.fillRecipientDataFor2_1ContainerIfNecessary(
                      docFiles, i, doc);

              // Vajadusel valideerime saadetavad XML dokumendid
              // validateXmlFiles(doc.getFiles());
              SendDocuments.validateXmlFiles(doc.getFiles());
              // Vajadusel kontrollime saadetavate .ddoc ja .bdoc failide
              // allkirjad üle
              SendDocuments.validateSignedFileSignatures(doc.getFiles());

              doc.setOrganizationID(user.getOrganizationID());
              doc.setContainerVersion(ContainerVersion.VERSION_2_1.toString());
              doc.setFolderID(senderTargetFolder);
              Calendar calendar = Calendar.getInstance();
              calendar.setTime(new Date());
              calendar.add(Calendar.DATE,
                      Settings.Server_DocumentDefaultLifetime);
              doc.setConservationDeadline(calendar.getTime());
              calendar = null;

              if ((doc.getSendingList() != null)) {
                  Sending tmpSending;
                  for (int j = 0; j < doc.getSendingList().size(); ++j) {
                      tmpSending = doc.getSendingList().get(j);
                      if (Settings.Server_DocumentSenderMustMatchXroadHeader) {
                          if ((tmpSending.getSender() != null)
                                  && (tmpSending.getSender()
                                          .getOrganizationID() != user
                                          .getOrganizationID())
                                  && (tmpSending.getProxy() != null)
                                  && (tmpSending.getProxy()
                                          .getOrganizationID() != user
                                          .getOrganizationID())) {
                              throw new ContainerValidationException(
                                      CommonStructures.VIGA_SAATJA_ASUTUSED_ERINEVAD);
                          }
                      }
                      // remove unneeded recipient. DVK adds all recipients
                      // from capsule, DHX sends to only
                      // one recipient
                      ArrayList<Recipient> recipients = new ArrayList<Recipient>();
                      for (int k = 0; k < tmpSending.getRecipients().size(); ++k) {
                          Recipient recipient = tmpSending.getRecipients()
                                  .get(k);
                          if (recipient.getOrganizationID() == recipientAsutus
                                  .getId()) {
                              recipient.setDhxExternalConsignmentId(document
                                      .getExternalConsignmentId());
                              recipients.add(recipient);
                          }
                      }
                      if (recipients.isEmpty()) {
                          throw new DhxException(
                                  DhxExceptionEnum.WRONG_RECIPIENT,
                                  "Unable to find recipient among capsule recipients added by DVK");
                      }
                      tmpSending.setRecipients(recipients);
                      if (validationFault != null) {
                          for (int k = 0; k < tmpSending.getRecipients()
                                  .size(); ++k) {
                              tmpSending.getRecipients().get(k)
                                      .setFault(validationFault);
                          }
                      }
                  }
              }
              serverDocuments.add(doc);
              doc.addToDB(conn, xroadHeader);

          }
      } catch (ContainerValidationException fault) {
          log.error(fault.getMessage(), fault);
          throw new DhxException(DhxExceptionEnum.CAPSULE_VALIDATION_ERROR,
                  "Error occured while recieving the document. "
                          + fault.getMessage(), fault);
      } catch(DhxException ex){
          throw ex;
      }catch (Exception fault) {
          log.error(fault.getMessage(), fault);
          throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
                  "Error occured while recieving the document. "
                          + fault.getMessage(), fault);
      } finally {
          CommonMethods.safeCloseDatabaseConnection(conn);
          conn = null;
          for (int i = 0; i < serverDocuments.size(); ++i) {
              (new File(serverDocuments.get(i).getFilePath())).delete();
          }
      }

      return String.valueOf(serverDocuments.get(0).getId());*/

  }


  /*
   * public abstract List<DhxRepresentee> getRepresentationList() throws DhxException;
   */

  public List<InternalXroadMember> getAdresseeList() throws DhxException {
    if (members == null) {
      members = new ArrayList<InternalXroadMember>();
      List<Organisation> orgs = organisationRepository.findByIsActive(true);
      for(Organisation org : orgs) {
        members.add(getInternalXroadMemberFromOrganisation(org));
      }
    }
    return members;
  }
  
  private InternalXroadMember getInternalXroadMemberFromOrganisation (Organisation org) {
    Organisation mainOrg = org;
    DhxRepresentee representee = null;
    if(org.getRepresentor() != null) {
      mainOrg = org.getRepresentor();
      representee = new DhxRepresentee(org.getRegistrationCode(), org.getRepresenteeStart(), org.getRepresenteeEnd(), org.getName(), org.getSubSystem());
    }
    InternalXroadMember member = new InternalXroadMember(mainOrg.getXroadInstance(), mainOrg.getMemberClass(), mainOrg.getRegistrationCode(),
      mainOrg.getSubSystem(), mainOrg.getName(), representee);
    return member;    
  }

  private Organisation getOrganisationFromInternalXroadMember(InternalXroadMember member)
      throws DhxException {
    Boolean newMember = false;
    Organisation organisation =
        organisationRepository.findByRegistrationCodeAndSubSystem(member.getMemberCode(),
            member.getSubsystemCode());
    if (organisation == null) {
      newMember = true;
      organisation = new Organisation();
    }
    organisation.setIsActive(true);
    organisation.setMemberClass(member.getMemberClass());
    organisation.setName(member.getName());
    organisation.setRegistrationCode(member.getMemberCode());
    organisation.setSubSystem(member.getSubsystemCode());
    organisation.setXroadInstance(member.getXroadInstance());
    if (member.getRepresentee() != null) {
      if (newMember) {
        // we cannot insert new representor with representee
        throw new DhxException(DhxExceptionEnum.DATA_ERROR,
            "Trying to insert prepresentee, but representor is not in database! ");
      }
      Organisation representeeOrganisation =
          organisationRepository.findByRegistrationCodeAndSubSystem(member.getRepresentee()
              .getRepresenteeCode(), member.getRepresentee().getRepresenteeSystem());
      if (representeeOrganisation == null) {
        representeeOrganisation = new Organisation();
      }
      representeeOrganisation.setIsActive(true);
      representeeOrganisation.setName(member.getRepresentee().getRepresenteeName());
      representeeOrganisation.setRegistrationCode(member.getRepresentee().getRepresenteeCode());
      representeeOrganisation.setSubSystem(member.getRepresentee().getRepresenteeSystem());
      if(member.getRepresentee().getStartDate() != null) {
        representeeOrganisation.setRepresenteeStart(new Timestamp(member.getRepresentee().getStartDate().getTime()));
      }
      if(member.getRepresentee().getEndDate() != null) {
        representeeOrganisation.setRepresenteeEnd(new Timestamp(member.getRepresentee().getEndDate().getTime()));
      }
      representeeOrganisation.setRepresentor(organisation);
     // organisation.addRepresentee(representeeOrganisation);
      organisation = representeeOrganisation;
    }
    return organisation;
  }

  @Override
  public void saveAddresseeList(List<InternalXroadMember> members)
      throws DhxException {
    updateNotActiveAdressees(members);
    List<Organisation> organisations = new ArrayList<Organisation>();
    for (InternalXroadMember member : members) {
      if (member.getRepresentee() == null) {
        organisations.add(getOrganisationFromInternalXroadMember(member));
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
        organisations.add(getOrganisationFromInternalXroadMember(member));
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
          recipient.setSendingEnd(new Timestamp( (new Date()).getTime()));
          if (docResponse.getFault() == null) {
              log.debug("Document was succesfuly sent to DHX");
              recipient.setDhxExternalReceiptId(docResponse
                      .getReceiptId());
              //recipient.setRecipientStatusId(11);
              Classificator status = classificatorRepository.findByName(StatusEnum.RECEIVED.getClassificatorName());
              recipient.setStatus(status);
          } else {
              log.debug("Fault occured while sending document to DHX");
              log.debug("All attempts to send documents were done. Saving document as failed.");
              Classificator status = classificatorRepository.findByName(StatusEnum.FAILED.getClassificatorName());
              //recipient.setRecipientStatusId(5);
              recipient.setStatus(status);
              //recipient.setSendingEndDate(new Date());
              String faultString = "";
              if(retryResults != null && retryResults.size()>0) {
                  faultString = faultString + " Total retries count: " + retryResults.size()
                          + " Results for individual retries: ";
                  for(AsyncDhxSendDocumentResult result : retryResults) {
                      faultString = faultString + "\n Retry date: " +  result.getTryDate()
                              + " Technical error:";
                      if (result.getResult().getOccuredException() != null ) {
                          faultString = faultString + " Error message:" + result.getResult().getOccuredException().getMessage()
                                  +  " Stacktrace: " + ExceptionUtils.getStackTrace(result.getResult().getOccuredException());        
                      }
                  }
              }
              faultString = docResponse.getFault()
                      .getFaultString() + faultString;
              recipient.setFaultCode(docResponse.getFault()
                      .getFaultCode());
              recipient.setFaultString(docResponse.getFault().getFaultString());
              recipient.setFaultDetail(faultString.substring(0, (faultString.length()>1900?1900:faultString.length())));
          }   
          recipientRepository.save(recipient);
      }
      catch(Exception ex) {
          log.error("Error occured while saving send results. " + ex.getMessage(), ex);
      } finally {
      }
  }

}

package ee.ria.dhx.server.scheduler;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.service.SoapService;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler class, that does periodical jobs.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
@Component
public class DhxServerScheduler {

  @Autowired
  SoapService soapService;


  @Value("${dhx.server.delete-old-documents}")
  @Setter
  String deleteOldDocuments;

  /**
   * Sends documents periodically.
   * 
   * @throws DhxException - thrown if error occures while sending document
   */
  @Scheduled(cron = "${dhx.server.send-to-dhx}")
  public void sendDvkDocuments() throws DhxException {
    log.debug("sending documents to DHX automatically.");
    soapService.sendDocumentsToDhx();
  }


  /**
   * Deletes documents periodically.
   * 
   * @throws DhxException - thrown if error occures while deleteing document
   */
  @Scheduled(cron = "${dhx.server.delete-old-documents-freq}")
  public void deleteOldDocuments() throws DhxException {
    log.debug("deleting documents to DHX automatically.");
    if (deleteOldDocuments.equalsIgnoreCase("delete-all")
        || deleteOldDocuments.equalsIgnoreCase("delete-content")) {
      soapService.deleteOldDocuments(deleteOldDocuments.equalsIgnoreCase("delete-all"));
    } else {
      log.debug("Document deleting is disabled.");
    }

  }
}

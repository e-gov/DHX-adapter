package ee.ria.dhx.server.scheduler;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.service.SoapService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
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
public class DocumentSenderScheduler {

  @Autowired
  SoapService dhxDocumentService;

  /**
   * Sends documents periodically.
   * 
   * @throws DhxException - thrown if error occures while sending document
   */
  @Scheduled(cron = "${dhx.server.send-to-dhx}")
  public void sendDvkDocuments() throws DhxException {
    log.debug("sending documents to DHX automatically.");
    dhxDocumentService.sendDocumentsToDhx();
  }


}

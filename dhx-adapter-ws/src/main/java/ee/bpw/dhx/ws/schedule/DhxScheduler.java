package ee.bpw.dhx.ws.schedule;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.ws.service.AddressService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@Configurable
public class DhxScheduler {

  @Autowired
  AddressService addressService;

  /**
   * periodically renews adress list.
   */
  @Scheduled(cron = "${address-renew-timeout}")
  public void renewAddressList() {
    try {
      log.debug("updating address DHX list automatically");
      addressService.renewAddressList();
    } catch (DhxException ex) {
      log.error(
          "Error occured while renewing addresslist. "
              + ex.getMessage(), ex);
    }
  }

}

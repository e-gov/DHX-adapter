package ee.bpw.dhx.ws.service;

import ee.bpw.dhx.exception.DhxException;
import ee.bpw.dhx.model.OutgoingDhxPackage;

import java.util.List;

public interface AsyncDhxPackageService {

  /**
   * Send package. Package is sent to recipient defined in outgoingPackage. Method is asynchronous,
   * therefore not returning anything, but after package is sent, callback method will be called.
   * 
   * @param outgoingPackage - package to send
   * @throws DhxException - thrown if error occurs while sending document
   */
  public void sendPackage(OutgoingDhxPackage outgoingPackage)
      throws DhxException;

  /**
   * Send package. Package is sent to recipient defined in outgoingPackage. Method is asynchronous,
   * therefore not returning anything, but after package is sent, callback method will be called.
   * Every package is sent to recipient defined in it. If package sending gets exception, then send
   * document result with fault in it is created, sending will be continued anyway. Callback is
   * called for every package separately.
   * 
   * @param outgoingPackages - package to send
   * @throws DhxException - thrown if error occurs while sending document
   */
  public void sendMultiplePackages(List<OutgoingDhxPackage> outgoingPackages)
      throws DhxException;

}

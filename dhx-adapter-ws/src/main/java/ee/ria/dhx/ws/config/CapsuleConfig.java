package ee.ria.dhx.ws.config;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.CapsuleAdressee;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer.Transport.DecRecipient;
import ee.ria.dhx.util.CapsuleVersionEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

@Configuration
public class CapsuleConfig {

  @Value("${dhx.xsd.capsule-xsd-file21:jar://Dvk_kapsel_vers_2_1_eng_est.xsd}")
  private String capsuleXsdFile21;

  @Value("${dhx.xsd.capsule-xsd-file21:V21}")
  private String currentCapsuleVersionStr;

  private CapsuleVersionEnum currentCapsuleVersion;

  @Autowired
  Environment env;

  /**
   * Automatically initialize properties.
   */
  @PostConstruct
  public void init() {
    if (currentCapsuleVersionStr != null) {
      setCurrentCapsuleVersion(CapsuleVersionEnum
          .valueOf(currentCapsuleVersionStr));
    }
  }

  public String getCurrentXsd() throws DhxException {
    return getXsdForVersion(currentCapsuleVersion);
  }

  /**
   * Method finds config parameter which contains link to XSD for given version.
   * 
   * @param version - version for which to find XSD
   * @return - link to XSD file for given version
   * @throws DhxException - thrown then no XSD file is defined for given version
   */
  public String getXsdForVersion(CapsuleVersionEnum version)
      throws DhxException {
    if (version == null) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Unable to find XSD file for given verion. version: null");
    }
    switch (version) {
      case V21:
        return getCapsuleXsdFile21();
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find XSD file for given verion. version:"
                + version.toString());
    }
  }

  /**
   * Method to find adresssees from container. Method returns adressees for every existing version
   * of the container, bacause service which uses that method does not know anything about container
   * and just needs adressees defined in it. Given implementation is able to find adressees for
   * capsule version 2.1
   * 
   * @param containerObject - container(capsule) object from which to find adressees
   * @return - list of the adresssees
   * @throws DhxException - thrown adressees parsing is not defined for given object (capsule
   *         version)
   */
  public List<CapsuleAdressee> getAdresseesFromContainer(
      Object containerObject) throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum
        .forClass(containerObject.getClass());
    switch (version) {
      case V21:
        List<CapsuleAdressee> adressees = new ArrayList<CapsuleAdressee>();
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getTransport() != null
            && container.getTransport().getDecRecipient() != null
            && container.getTransport().getDecRecipient().size() > 0) {
          for (DecRecipient recipient : container.getTransport()
              .getDecRecipient()) {
            adressees.add(new CapsuleAdressee(recipient
                .getOrganisationCode(), recipient.getPersonalIdCode(), recipient
                .getStructuralUnit()));
          }
          return adressees;
        }
        return null;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:"
                + version.toString());
    }
  }

  /**
   * Method to find sender from container. Method returns sender for every existing version of the
   * container, bacause service which uses that method does not know anything about container and
   * just needs sender defined in it. Given implementation is able to find sender for capsule
   * version 2.1
   * 
   * @param containerObject - container(capsule) object from which to find adressees
   * @return - sender object
   * @throws DhxException - thrown adressees parsing is not defined for given object (capsule
   *         version)
   */
  public CapsuleAdressee getSenderFromContainer(Object containerObject)
      throws DhxException {
    CapsuleVersionEnum version = CapsuleVersionEnum
        .forClass(containerObject.getClass());
    switch (version) {
      case V21:
        DecContainer container = (DecContainer) containerObject;
        if (container != null && container.getTransport() != null
            && container.getTransport().getDecSender() != null) {
          return new CapsuleAdressee(container.getTransport()
              .getDecSender().getOrganisationCode(), container.getTransport()
              .getDecSender().getPersonalIdCode(), container.getTransport()
              .getDecSender().getStructuralUnit());
        }
        return null;
      default:
        throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
            "Unable to find adressees for given verion. version:"
                + version.toString());
    }
  }

  /**
   * by default jar://Dvk_kapsel_vers_2_1_eng_est.xsd
   * 
   * @return the location of the capsule version 2.1 XSD
   */
  public String getCapsuleXsdFile21() {
    return capsuleXsdFile21;
  }

  /**
   * be default jar://Dvk_kapsel_vers_2_1_eng_est.xsd
   * 
   * @param capsuleXsdFile21 the location of the capsule version 2.1 XSD
   */
  public void setCapsuleXsdFile21(String capsuleXsdFile21) {
    this.capsuleXsdFile21 = capsuleXsdFile21;
  }

  /**
   * by default V21.
   * 
   * @return the current version of the capsule to send and receive
   */
  public String getCurrentCapsuleVersionStr() {
    return currentCapsuleVersionStr;
  }

  /**
   * by default V21.
   * 
   * @param currentCapsuleVersionStr current version of the capsule to send and receive
   */
  public void setCurrentCapsuleVersionStr(String currentCapsuleVersionStr) {
    this.currentCapsuleVersionStr = currentCapsuleVersionStr;
  }

  /**
   * Return the currentCapsuleVersion.
   * @return the currentCapsuleVersion enumeration of the current version of the capsule to send and
   *         receive
   */
  public CapsuleVersionEnum getCurrentCapsuleVersion() {
    return currentCapsuleVersion;
  }

  /**
   * Sets the currentCapsuleVersion.
   * @param currentCapsuleVersion enumeration of the current version of the capsule to send and
   *        receive
   */
  public void setCurrentCapsuleVersion(CapsuleVersionEnum currentCapsuleVersion) {
    this.currentCapsuleVersion = currentCapsuleVersion;
  }

}

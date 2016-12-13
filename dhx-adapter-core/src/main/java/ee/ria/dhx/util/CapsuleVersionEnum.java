package ee.ria.dhx.util;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import lombok.extern.slf4j.Slf4j;

/**
 * Enumeration which contains all capsule versions that DHX application can read.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public enum CapsuleVersionEnum {

  V21(DecContainer.class);

  private CapsuleVersionEnum(Class<? extends Object> containerClass) {
    this.containerClass = containerClass;
  }

  private Class<? extends Object> containerClass;

  public Class<? extends Object> getContainerClass() {
    return containerClass;
  }

  /**
   * Method finds XSdVersion for given Class. Root Class needs to be given to find version.
   * 
   * @param containerClass - root class of the container
   * @return - version of the container
   * @throws DhxException - thrown if error occurs while searching version
   */
  public static CapsuleVersionEnum forClass(Class<? extends Object> containerClass)
      throws DhxException {
    if (containerClass != null) {
      for (CapsuleVersionEnum version : CapsuleVersionEnum.values()) {
        if (containerClass.equals(version.getContainerClass())) {
          log.debug("Found XSD version for class. containerClass: {}", containerClass.toString()
              + " version:" + version.toString());
          return version;
        }
      }
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Unknown class. No XSD version is found for that class. Class: "
              + containerClass.toString());
    }
    return null;
  }

}

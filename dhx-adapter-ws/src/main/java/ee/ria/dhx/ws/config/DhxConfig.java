package ee.ria.dhx.ws.config;

import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


@Configuration
/**
 * Main configuration of DHX webservice application
 * 
 * @author Aleksei Kokarev
 *
 */
public class DhxConfig {

  private final String marshallContextSeparator = ":";
  private final String separator = ",";

  @Value("${dhx.capsule-validate:true}")
  private Boolean capsuleValidate = true;

  @Value("${dhx.check-recipient:true}")
  private Boolean checkRecipient = true;

  @Value("${dhx.check-sender:true}")
  private Boolean checkSender = true;

  @Value("${dhx.check-filesize:false}")
  private Boolean checkFilesize = false;

  @Value("${dhx.check-duplicate:true}")
  private Boolean checkDuplicate = true;

  @Value("${dhx.parse-capsule:true}")
  private Boolean parseCapsule = true;

  // list of timeout in seconds, delimited by comma.
  @Value("${dhx.document-resend-template:30,120,1200}")
  private String documentResendTemplate;

  @Value("${dhx.wsdl-file:dhx.wsdl}")
  private String wsdlFile;

  @Value("${dhx.protocol-version:1.0}")
  private String protocolVersion;

  @Value("${dhx.check-dhx-version:true}")
  private Boolean checkDhxVersion;

  @Value("${dhx.accepted-dhx-protocol-versions:1.0}")
  private String acceptedDhxProtocolVersions;

  @Value("${dhx.marshall-context:ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1"
      + ":ee.ria.dhx.types.eu.x_road.dhx.producer:ee.ria.dhx.types.eu.x_road.xsd.identifiers"
      + ":ee.ria.dhx.types.eu.x_road.xsd.representation:ee.ria.dhx.types.eu.x_road.xsd.xroad}")
  private String marshallContext;

  @Value("${dhx.renew-address-list-on-startup:true}")
  private Boolean renewAddresslistOnStartup;

  private JAXBContext jaxbContext;

  private String[] marshallContextAsList;

  private List<Integer> documentResendTimes;

  /**
   * Method return marshalling context as list.
   * 
   * @return array of package names for marshaller
   */
  public String[] getMarshallContextAsList() {
    if (marshallContextAsList == null) {
      String[] contextArray = marshallContext
          .split(marshallContextSeparator);
      marshallContextAsList = contextArray;
    }
    return marshallContextAsList;
  }

  /**
   * Method return marshalling context as list.
   * 
   * @return array of package names for marshaller
   */
  public List<Integer> getDocumentResendTimes() {
    if (documentResendTimes == null) {
      String[] timesArray = documentResendTemplate.split(separator);
      Integer[] times = new Integer[timesArray.length];
      for (int i = 0; i < timesArray.length; i++) {
        String time = timesArray[i];
        Integer timeInt = Integer.parseInt(time);
        times[i] = timeInt;
      }
      documentResendTimes = Arrays.asList(times);
    }
    return documentResendTimes;
  }

  /**
   * Returns the list of DHX protocol versions that are accepted by this implementation, separated
   * by comma.
   * 
   * @return list of DHX protocol versions that are accepted by this implementation, separated by
   *         comma
   */
  public String getAcceptedDhxProtocolVersions() {
    return "," + acceptedDhxProtocolVersions + ",";
  }

  /**
   * Sets marshaller bean.
   * 
   * @return marshaller
   * @throws JAXBException - thrown if error occurs
   */
  @Bean
  public JAXBContext getJaxbContext() throws JAXBException {
    if (this.jaxbContext == null) {
      this.jaxbContext = JAXBContext.newInstance(marshallContext);
    }
    return jaxbContext;
  }


  /**
   * by default true.
   * 
   * @return does capsule needs validation against xsd
   */
  public Boolean getCapsuleValidate() {
    return capsuleValidate;
  }

  /**
   * by default true.
   * 
   * @param capsuleValidate does capsule needs validation against xsd
   */
  public void setCapsuleValidate(Boolean capsuleValidate) {
    this.capsuleValidate = capsuleValidate;
  }

  /**
   * by default true.
   * 
   * @return is recipient check needed
   */
  public Boolean getCheckRecipient() {
    return checkRecipient;
  }

  /**
   * by default true.
   * 
   * @param checkRecipient is recipient check needed
   */
  public void setCheckRecipient(Boolean checkRecipient) {
    this.checkRecipient = checkRecipient;
  }

  /**
   * by default true.
   * 
   * @return is sender check needed
   */
  public Boolean getCheckSender() {
    return checkSender;
  }

  /**
   * by default true.
   * 
   * @param checkSender is sender check needed
   */
  public void setCheckSender(Boolean checkSender) {
    this.checkSender = checkSender;
  }

  /**
   * by default true.
   * 
   * @return is check for duplicate documents needed
   */
  public Boolean getCheckDuplicate() {
    return checkDuplicate;
  }

  /**
   * by default true.
   * 
   * @param checkDuplicate is check for duplicate documents needed
   */
  public void setCheckDuplicate(Boolean checkDuplicate) {
    this.checkDuplicate = checkDuplicate;
  }

  /**
   * by default true.
   * 
   * @return is capsule parsing needed when receiving document
   */
  public Boolean getParseCapsule() {
    return parseCapsule;
  }

  /**
   * by default true.
   * 
   * @param parseCapsule is capsule parsing needed when receiving document
   */
  public void setParseCapsule(Boolean parseCapsule) {
    this.parseCapsule = parseCapsule;
  }

  /**
   * documentResendTemplate represents how many times and with how big timeouts document will be
   * resent. For example 5,10,15 means that after first failed try application will wait 5 seconds
   * and try again. If second attempt fails, then there will be 10 seconds timeout before third
   * attempt and so on. by default 30,120,1200.
   * 
   * @return the documentResendTemplate
   */
  public String getDocumentResendTemplate() {
    return documentResendTemplate;
  }

  /**
   * documentResendTemplate represents how many times and with how big timeouts document will be
   * resent. For example 5,10,15 means that after first failed try application will wait 5 seconds
   * and try again. If second attempt fails, then there will be 10 seconds timeout before third
   * attempt and so on. by default 30,120,1200.
   * 
   * @param documentResendTemplate the documentResendTemplate to set
   */
  public void setDocumentResendTemplate(String documentResendTemplate) {
    this.documentResendTemplate = documentResendTemplate;
  }

  /**
   * by default dhx.wsdl.
   * 
   * @return the name of the wsdl file in classpath.
   */
  public String getWsdlFile() {
    return wsdlFile;
  }

  /**
   * by default dhx.wsdl.
   * 
   * @param wsdlFile the name of the wsdl file in classpath to set.
   */
  public void setWsdlFile(String wsdlFile) {
    this.wsdlFile = wsdlFile;
  }

  /**
   * by default 1.0.
   * 
   * @return the currently implemented DHX protocol version
   */
  public String getProtocolVersion() {
    return protocolVersion;
  }

  /**
   * by default 1.0.
   * 
   * @param protocolVersion the currently implemented DHX protocol version to set
   */
  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  /**
   * by default true.
   * 
   * @return is protocol version check of the document sender enabled.
   */
  public Boolean getCheckDhxVersion() {
    return checkDhxVersion;
  }

  /**
   * by default true.
   * 
   * @param checkDhxVersion is protocol version check of the document sender enabled.
   */
  public void setCheckDhxVersion(Boolean checkDhxVersion) {
    this.checkDhxVersion = checkDhxVersion;
  }

  /**
   * by default
   * ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1:ee.ria.dhx.types.eu.x_road.dhx.producer:
   * ee
   * .ria.dhx.types.eu.x_road.xsd.identifiers:ee.ria.dhx.types.eu.x_road.xsd.representation:ee.ria.
   * dhx.types.eu.x_road.xsd.xroad.
   * 
   * @return the packages used for marshalling and unmarshalling, separated by colon
   */
  public String getMarshallContext() {
    return marshallContext;
  }

  /**
   * by default
   * ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1:ee.ria.dhx.types.eu.x_road.dhx.producer:
   * ee
   * .ria.dhx.types.eu.x_road.xsd.identifiers:ee.ria.dhx.types.eu.x_road.xsd.representation:ee.ria.
   * dhx.types.eu.x_road.xsd.xroad.
   * 
   * @param marshallContext the packages used for marshalling and unmarshalling, separated by colon
   */
  public void setMarshallContext(String marshallContext) {
    this.marshallContext = marshallContext;
  }

  /**
   * by default true.
   * 
   * @return is address list needs to be renewed on startup
   */
  public Boolean getRenewAddresslistOnStartup() {
    return renewAddresslistOnStartup;
  }

  /**
   * by default true.
   * 
   * @param renewAddresslistOnStartup is address list needs to be renewed on startup
   */
  public void setRenewAddresslistOnStartup(Boolean renewAddresslistOnStartup) {
    this.renewAddresslistOnStartup = renewAddresslistOnStartup;
  }

  /**
   * Sets the list of DHX protocol versions that are accepted by this implementation, separated by
   * comma.
   * 
   * @param acceptedDhxProtocolVersions the list of DHX protocol versions that are accepted by this
   *        implementation, separated by comma
   */
  public void setAcceptedDhxProtocolVersions(String acceptedDhxProtocolVersions) {
    this.acceptedDhxProtocolVersions = acceptedDhxProtocolVersions;
  }


}

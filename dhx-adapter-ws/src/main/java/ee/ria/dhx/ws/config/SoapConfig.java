package ee.ria.dhx.ws.config;

import ee.ria.dhx.types.InternalXroadMember;

import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Getter
@Configuration
/**
 * Configuration parameters needed for SOAP services.
 * @author Aleksei Kokarev
 *
 */
public class SoapConfig {

  private final String separator = ",";

  @Value("${soap.targetnamespace :http://dhx.x-road.eu/producer}")
  String targetnamespace;

  @Value("${soap.security-server}")
  String securityServer;

  @Value("${soap.security-server-appender :/cgi-bin/consumer_proxy}")
  String securityServerAppender;

  @Value("${soap.xroad-instance}")
  String xroadInstance;

  @Value("${soap.member-class}")
  String memberClass;

  @Value("${soap.member-code}")
  String memberCode;

  @Value("${soap.default-subsystem:DHX}")
  String defaultSubsystem;

  @Value("${soap.protocol-version:4.0}")
  String protocolVersion;

  @Value("${soap.global-conf-location:verificationconf}")
  String globalConfLocation;

  @Value("${soap.global-conf-filename:shared-params.xml}")
  String globalConfFilename;

  @Value("${soap.dhx-representation-group-name:DHX vahendajad}")
  String dhxRepresentationGroupName;

  @Value("${soap.accepted-subsystems:DHX}")
  String acceptedSubsystems;

  /*
   * String serviceXroadInstance; String serviceMemberClass; String serviceSubsystem;
   */

  @Value("${soap.send-document-service-code:sendDocument}")
  String sendDocumentServiceCode;

  @Value("${soap.send-document-service-version:v1}")
  String sendDocumentServiceVersion;

  @Value("${soap.representatives-service-code:representationList}")
  String representativesServiceCode = "representationList";

  @Value("${soap.representatives-service-version:v1}")
  String representativesServiceVersion = "v1";

  @Value("${soap.connection-timeout:60000}")
  Integer connectionTimeout;

  @Value("${soap.read-timeout:120000}")
  Integer readTimeout;

  @Value("${soap.dhx-subsystem-prefix:DHX}")
  String dhxSubsystemPrefix;

  List<String> acceptedSubsystemsAsList;

  /**
   * Method returns subsystems list that are accepted. means that DHX documents sent to those
   * subsystems will be accepted.
   * 
   * @return - accepted subsystem list
   */
  public List<String> getAcceptedSubsystemsAsList() {
    if (acceptedSubsystemsAsList == null) {
      String[] contextArray = acceptedSubsystems.split(separator);
      acceptedSubsystemsAsList = Arrays.asList(contextArray);
    }
    return acceptedSubsystemsAsList;
  }

  public String getSecurityServerWithAppender() {
    return securityServer + securityServerAppender;
  }

  /**
   * Helper method to add prefix if no prefix found.
   * 
   * @param system - system name to add prefix to
   * @return - uppercase system with prefix added
   */
  public String addPrefixIfNeeded(String system) {
    if (system == null) {
      system = getDhxSubsystemPrefix();
    }
    if (!system.startsWith(getDhxSubsystemPrefix())) {
      system = getDhxSubsystemPrefix() + "." + system;
    }
    return system.toUpperCase();
  }

  /**
   * Method returns default xroad client. That client will be used as sender when sending documents.
   * 
   * @return - default client
   */
  public InternalXroadMember getDefaultClient() {
    InternalXroadMember client = new InternalXroadMember(
        getXroadInstance(), getMemberClass(), getMemberCode(),
        getDefaultSubsystem(), "", null);
    return client;
  }

}

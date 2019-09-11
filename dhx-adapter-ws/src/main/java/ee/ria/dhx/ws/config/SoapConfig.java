package ee.ria.dhx.ws.config;

import ee.ria.dhx.types.InternalXroadMember;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import java.net.URL;
import java.net.URLClassLoader;


import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
/**
 * Configuration parameters needed for SOAP services.
 * 
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

  @Value("${soap.http-timeout:300}")
  Integer httpTimeout;

  @Value("${soap.dhx-subsystem-prefix:DHX}")
  String dhxSubsystemPrefix;


  @Value("${soap.client-truststore-file}")
  String clientTruststoreFile;

  @Value("${soap.client-truststore-password}")
  String clientTruststorePassword;

  @Value("${soap.client-truststore-type:JKS}")
  String clientTruststoreType;

  @Value("${soap.client-keystore-file}")
  String clientKeystoreFile;

  @Value("${soap.client-keystore-password}")
  String clientKeystorePassword;
  
  @Value("${soap.client-keystore-type:JKS}")
  String clientKeystoreType;
  
  public String getClientTrustStoreFile() {
    return clientTruststoreFile;
  }
  
  public String getClientTrustStorePassword() {
    return clientTruststorePassword;
  }
  
  public String getClientTrustStoreType() {
    return clientTruststoreType;
  }

  public String getClientKeyStoreFile() {
    return clientKeystoreFile;
  }
  
  public String getClientKeyStorePassword() {
    return clientKeystorePassword;
  }
  
  public String getClientKeyStoreType() {
    return clientKeystoreType;
  }

  
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

  @Bean
  public KeyStore clientTrustStore () throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
    KeyStore trustStore = null;
    if (getSecurityServer().toUpperCase().startsWith("HTTPS")) {
      trustStore = KeyStore.getInstance(getClientTrustStoreType());
      File trustStoreFile = new File(getClientTrustStoreFile());
      trustStore.load(new FileInputStream(trustStoreFile), getClientTrustStorePassword().toCharArray());
    }
    return trustStore;
  }

  @Bean
  public KeyStore clientKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
    KeyStore keyStore = null;
    if (getSecurityServer().toUpperCase().startsWith("HTTPS")) {
      keyStore = KeyStore.getInstance(getClientKeyStoreType());
      InputStream keystoreIn = new FileInputStream(getClientKeyStoreFile());
      keyStore.load(keystoreIn, getClientKeyStorePassword().toCharArray());
    }
    return keyStore;
  }

  @Bean
  public SSLContext sslContext(KeyStore clientTrustStore, KeyStore clientKeyStore) throws KeyStoreException,
          NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
    SSLContextBuilder sslContextBuilder = SSLContexts.custom();
    TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
    if (getSecurityServer().toUpperCase().startsWith("HTTPS")) {
      if (clientKeyStore != null) {
        sslContextBuilder.loadKeyMaterial(clientKeyStore, getClientTrustStorePassword().toCharArray());
      }
      sslContextBuilder.loadTrustMaterial(clientTrustStore, acceptingTrustStrategy);
    }
    return sslContextBuilder.build();
  }

  @Bean
  public SSLConnectionSocketFactory sslSocketFactory(SSLContext sslContext) {
    return new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1.1", "TLSv1.2"}, null,
            SSLConnectionSocketFactory.getDefaultHostnameVerifier());
  }

  @Bean
  public RequestConfig requestConfig() {
    return RequestConfig.custom()
            .setConnectionRequestTimeout(getConnectionTimeout())
            .setSocketTimeout(getReadTimeout())
            .build();
  }

  @Bean
  public HttpClient httpClient() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {
    return HttpClientBuilder
            .create()
            .setSSLSocketFactory(sslSocketFactory(sslContext(clientTrustStore(), clientKeyStore())))
            .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

              public long getKeepAliveDuration(HttpResponse response, HttpContext context) {

                HeaderElementIterator it = new BasicHeaderElementIterator(
                        response.headerIterator(HTTP.CONN_KEEP_ALIVE));

                while (it.hasNext()) {
                  HeaderElement he = it.nextElement();
                  String param = he.getName();
                  String value = he.getValue();
                  if (value != null && param.equalsIgnoreCase("timeout")) {

                    try {
                      return Long.parseLong(value) * 1000;
                    } catch(NumberFormatException ignore) {
                    }

                  }
                }
                // otherwise keep alive for <soap.http-timeout> seconds
                return getHttpTimeout() * 1000;
              }
            })
            .setDefaultRequestConfig(requestConfig())
            .build();
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

  /**
   * by default http://dhx.x-road.eu/producer
   * 
   * @return the targetnamespace
   */
  public String getTargetnamespace() {
    return targetnamespace;
  }

  /**
   * by default http://dhx.x-road.eu/producer
   * 
   * @param targetnamespace the targetnamespace to set
   */
  public void setTargetnamespace(String targetnamespace) {
    this.targetnamespace = targetnamespace;
  }

  /**
   * Returns the location of Xroad securityServer.
   * 
   * @return the location of Xroad securityServer.
   */
  public String getSecurityServer() {
    return securityServer;
  }

  /**
   * @param securityServer the location of Xroad securityServer to set. Only address is needed,
   *        without additional URL paths like /cgi-bin/consumer_proxy
   */
  public void setSecurityServer(String securityServer) {
    this.securityServer = securityServer;
  }

  /**
   * by default /cgi-bin/consumer_proxy.
   * 
   * @return the URL path needed when doing SOAP requests to securityServer
   */
  public String getSecurityServerAppender() {
    return securityServerAppender;
  }

  /**
   * by default /cgi-bin/consumer_proxy.
   * 
   * @param securityServerAppender the URL path needed when doing SOAP requests to securityServer to
   *        set
   */
  public void setSecurityServerAppender(String securityServerAppender) {
    this.securityServerAppender = securityServerAppender;
  }

  /**
   * Used as Xroad default client xroadInstance, means that if xroadInstance is not provided, that
   * value will be used.
   * 
   * @return the xroadInstance
   */
  public String getXroadInstance() {
    return xroadInstance;
  }

  /**
   * Used as Xroad default client xroadInstance, means that if xroadInstance is not provided, that
   * value will be used.
   * 
   * @param xroadInstance the xroadInstance to set
   */
  public void setXroadInstance(String xroadInstance) {
    this.xroadInstance = xroadInstance;
  }

  /**
   * Used as Xroad default client memberClass, means that if memberClass is not provided, that value
   * will be used.
   * 
   * @return the Xroad memberClass
   */
  public String getMemberClass() {
    return memberClass;
  }

  /**
   * Used as Xroad default client memberClass, means that if memberClass is not provided, that value
   * will be used.
   * 
   * @param memberClass the Xroad memberClass to set
   */
  public void setMemberClass(String memberClass) {
    this.memberClass = memberClass;
  }

  /**
   * Used as Xroad default client memberCode, means that if memberCode is not provided, that value
   * will be used.
   * 
   * @return the Xroad memberCode
   */
  public String getMemberCode() {
    return memberCode;
  }

  /**
   * Used as Xroad default client memberCode, means that if memberCode is not provided, that value
   * will be used.
   * 
   * @param memberCode the Xroad memberCode to set
   */
  public void setMemberCode(String memberCode) {
    this.memberCode = memberCode;
  }

  /**
   * Used as Xroad default client subsystem, means that if subsystem is not provided, that value
   * will be used. by default DHX.
   * 
   * @return the defaultSubsystem
   */
  public String getDefaultSubsystem() {
    return defaultSubsystem;
  }

  /**
   * Used as Xroad default client subsystem, means that if subsystem is not provided, that value
   * will be used. by default DHX.
   * 
   * @param defaultSubsystem the defaultSubsystem to set
   */
  public void setDefaultSubsystem(String defaultSubsystem) {
    this.defaultSubsystem = defaultSubsystem;
  }

  /**
   * by default 4.0.
   * 
   * @return the protocolVersion
   */
  public String getProtocolVersion() {
    return protocolVersion;
  }

  /**
   * by default 4.0.
   * 
   * @param protocolVersion the protocolVersion to set
   */
  public void setProtocolVersion(String protocolVersion) {
    this.protocolVersion = protocolVersion;
  }

  /**
   * by default verificationconf.
   * 
   * @return the globalConfLocation in the security server
   */
  public String getGlobalConfLocation() {
    return globalConfLocation;
  }

  /**
   * by default verificationconf.
   * 
   * @param globalConfLocation the globalConfLocation location
   */
  public void setGlobalConfLocation(String globalConfLocation) {
    this.globalConfLocation = globalConfLocation;
  }

  /**
   * by default shared-params.xml.
   * 
   * @return the name of the file with global configuration
   */
  public String getGlobalConfFilename() {
    return globalConfFilename;
  }

  /**
   * by default shared-params.xml.
   * 
   * @param globalConfFilename the name of the file with global configuration to set
   */
  public void setGlobalConfFilename(String globalConfFilename) {
    this.globalConfFilename = globalConfFilename;
  }

  
  /**
   * by default DHX vahendajad.
   * 
   * @return the name of the Xroad group caontaining DHX representors
   */
  public String getDhxRepresentationGroupName() {
    return dhxRepresentationGroupName;
  }

  /**
   * by default DHX vahendajad.
   * 
   * @param dhxRepresentationGroupName the name of the Xroad group caontaining DHX representors to
   *        set
   */
  public void setDhxRepresentationGroupName(String dhxRepresentationGroupName) {
    this.dhxRepresentationGroupName = dhxRepresentationGroupName;
  }

  /**
   * by default DHX.
   * 
   * @return the comma esparated list of accepted subsystems, in case if server accepts documents
   *         for several subsystems at a time
   */
  public String getAcceptedSubsystems() {
    return acceptedSubsystems;
  }

  /**
   * by default DHX.
   * 
   * @param acceptedSubsystems the comma esparated list of accepted subsystems, in case if server
   *        accepts documents for several subsystems at a time
   */
  public void setAcceptedSubsystems(String acceptedSubsystems) {
    this.acceptedSubsystems = acceptedSubsystems;
  }

  /**
   * by default sendDocument.
   * 
   * @return the Xroad service code of the sendDocument service
   */
  public String getSendDocumentServiceCode() {
    return sendDocumentServiceCode;
  }

  /**
   * by default sendDocument.
   * 
   * @param sendDocumentServiceCode the Xroad service code of the sendDocument service to set
   */
  public void setSendDocumentServiceCode(String sendDocumentServiceCode) {
    this.sendDocumentServiceCode = sendDocumentServiceCode;
  }

  /**
   * by default v1.
   * 
   * @return the version of the sendDocument Xroad service to send requests to.
   */
  public String getSendDocumentServiceVersion() {
    return sendDocumentServiceVersion;
  }

  /**
   * by default v1.
   * 
   * @param sendDocumentServiceVersion the version of the sendDocument Xroad service to send
   *        requests to.
   */
  public void setSendDocumentServiceVersion(String sendDocumentServiceVersion) {
    this.sendDocumentServiceVersion = sendDocumentServiceVersion;
  }

  /**
   * by default representationList.
   * 
   * @return the Xroad service code of the representationList service
   */
  public String getRepresentativesServiceCode() {
    return representativesServiceCode;
  }

  /**
   * by default representationList.
   * 
   * @param representativesServiceCode the Xroad service code of the representationList service to
   *        set
   */
  public void setRepresentativesServiceCode(String representativesServiceCode) {
    this.representativesServiceCode = representativesServiceCode;
  }

  /**
   * by default v1.
   * 
   * @return the version of the representationList Xroad service to send requests to.
   */
  public String getRepresentativesServiceVersion() {
    return representativesServiceVersion;
  }

  /**
   * by default v1.
   * 
   * @param representativesServiceVersion the version of the representationList Xroad service to
   *        send requests to.
   */
  public void setRepresentativesServiceVersion(String representativesServiceVersion) {
    this.representativesServiceVersion = representativesServiceVersion;
  }

  /**
   * by default 60000 .
   * 
   * @return the connectionTimeout of the SOAP requests in milliseconds
   */
  public Integer getConnectionTimeout() {
    return connectionTimeout;
  }

  /**
   * by default 60000 .
   * 
   * @param connectionTimeout the connectionTimeout of the SOAP requests in milliseconds to set
   */
  public void setConnectionTimeout(Integer connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  /**
   * by default 60000 .
   * 
   * @return the readTimeout of the SOAP requests in milliseconds
   */
  public Integer getReadTimeout() {
    return readTimeout;
  }

  /**
   * by default 120000 .
   * 
   * @param readTimeout the readTimeout of the SOAP requests in milliseconds to set
   */
  public void setReadTimeout(Integer readTimeout) {
    this.readTimeout = readTimeout;
  }


  /**
   * by default 300 seconds.
   * 
   * @return the httpTimeout of the SOAP requests in seconds
   */
  public Integer getHttpTimeout() {
    return httpTimeout;
  }

  /**
   * by default 300 seconds.
   * 
   * @param httpTimeout the readTimeout of the SOAP requests in seconds to set
   */
  public void setHttpTimeout(Integer httpTimeout) {
    this.httpTimeout = httpTimeout;
  }

  
  /**
   * by default DHX.
   * 
   * @return the prefix of the Xroad subsystems used for DHX
   */
  public String getDhxSubsystemPrefix() {
    return dhxSubsystemPrefix;
  }

  /**
   * by default DHX.
   * 
   * @param dhxSubsystemPrefix the prefix of the Xroad subsystems used for DHX to set
   */
  public void setDhxSubsystemPrefix(String dhxSubsystemPrefix) {
    this.dhxSubsystemPrefix = dhxSubsystemPrefix;
  }

  /**
   * Sets the acceptedSubsystemsAsList.
   * 
   * @param acceptedSubsystemsAsList the acceptedSubsystemsAsList to set
   */
  public void setAcceptedSubsystemsAsList(List<String> acceptedSubsystemsAsList) {
    this.acceptedSubsystemsAsList = acceptedSubsystemsAsList;
  }

  
  /**
   * Expand env vars.
   *
   * @param text the text
   * @return the string
   */
  public static String expandEnvVars(String text) {
      Map<String, String> envMap = System.getenv();
      for (Entry<String, String> entry : envMap.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue().replace('\\', '/');
          text = text.replaceAll("\\$\\{" + key + "\\}", value);
      }
      return text;
  }


  /**
   * Inits the.
   */
  @PostConstruct
  public void init() {
    
    log.info("SecurityServer: {}", getSecurityServer());
    log.info("javax.net.ssl.trustStore: {}", expandEnvVars(getClientTrustStoreFile()));
    log.info("javax.net.ssl.trustStorePassword: {}", getClientTrustStorePassword());
    log.info("javax.net.ssl.trustStoreType: {}", getClientTrustStoreType());

    log.info("javax.net.ssl.keyStore: {}", expandEnvVars(getClientKeyStoreFile()));
    log.info("javax.net.ssl.keyStorePassword: {}", getClientKeyStorePassword());
    log.info("javax.net.ssl.keyStoreType: {}", getClientKeyStoreType());

    ClassLoader cl = ClassLoader.getSystemClassLoader();

    URL[] urls = ((URLClassLoader)cl).getURLs();

    for(URL url: urls){
      log.info("SoapConfig.url: {}", url.getFile());
    }
    
    // setup truststore
    if (getSecurityServer().toLowerCase().startsWith("https")) {
        System.setProperty("javax.net.ssl.trustStore", expandEnvVars(getClientTrustStoreFile()));
        System.setProperty("javax.net.ssl.trustStorePassword", getClientTrustStorePassword());
        System.setProperty("javax.net.ssl.trustStoreType", getClientTrustStoreType());

        System.setProperty("javax.net.ssl.keyStore", expandEnvVars(getClientKeyStoreFile()));
        System.setProperty("javax.net.ssl.keyStorePassword", getClientKeyStorePassword());
        System.setProperty("javax.net.ssl.keyStoreType", getClientKeyStoreType());
    }
  }
  
}


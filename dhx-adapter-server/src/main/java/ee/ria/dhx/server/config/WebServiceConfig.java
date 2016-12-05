package ee.ria.dhx.server.config;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

import java.util.Properties;

import javax.xml.soap.SOAPException;

@EnableWs
@Configuration
public class WebServiceConfig {

  private class DhxClientSoapFaultTranslatorExceptionResolver
      extends SoapFaultMappingExceptionResolver {
    final Logger logger = LogManager.getLogger();

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
      logger.error("Error occured while using SOAP service." + ex.getMessage(), ex);
      super.customizeFault(endpoint, ex, fault);
    }
  }

  /**
   * Creates bean SoapFaultMappingExceptionResolver.
   * 
   * @return SoapFaultMappingExceptionResolver
   */
  @Bean
  public SoapFaultMappingExceptionResolver exceptionResolver() {
    SoapFaultMappingExceptionResolver exceptionResolver =
        new DhxClientSoapFaultTranslatorExceptionResolver();
    SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
    faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
    exceptionResolver.setDefaultFault(faultDefinition);

    Properties errorMappings = new Properties();
    errorMappings.setProperty(Exception.class.getName(), SoapFaultDefinition.SERVER.toString());
    errorMappings.setProperty(SOAPException.class.getName(),
        SoapFaultDefinition.SERVER.toString());
    exceptionResolver.setExceptionMappings(errorMappings);
    exceptionResolver.setOrder(1);
    return exceptionResolver;
  }

}

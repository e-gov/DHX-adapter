package ee.ria.dhx.ws.config.endpoint;

import ee.ria.dhx.ws.config.DhxConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.config.annotation.WsConfigurationSupport;
import org.springframework.ws.server.endpoint.adapter.DefaultMethodEndpointAdapter;
import org.springframework.ws.server.endpoint.adapter.method.MarshallingPayloadMethodProcessor;
import org.springframework.ws.server.endpoint.adapter.method.MessageContextMethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodArgumentResolver;
import org.springframework.ws.server.endpoint.adapter.method.MethodReturnValueHandler;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates beans needed for DHX webservices.
 * 
 * @author Aleksei Kokarev
 *
 */
@Configuration
@ComponentScan(basePackages = "ee.ria.dhx.ws.endpoint")
public class DhxEndpointConfig extends WsConfigurationSupport {

  @Autowired
  DhxConfig config;

  @Autowired
  Jaxb2Marshaller marshaller;

  /**
   * Injects DefaultMethodEndpointAdapter which supports SOAP message attachments. Sets proper
   * marshaller. That bean might iterfere with another same bean if it is defined(in that case most
   * probably code need to be changed to define single bean which will staisfy both needs).
   * 
   * @return DefaultMethodEndpointAdapter
   */
  @Bean(name = "dhxMethodEndpointAdapter")
  public DefaultMethodEndpointAdapter dhxMethodEndpointAdapter(List<MarshallingPayloadMethodProcessor> dhxMethodProcessors) {
    final List<MethodArgumentResolver> argumentResolvers = new ArrayList<MethodArgumentResolver>(dhxMethodProcessors);
    final List<MethodReturnValueHandler> returnValueHandlers = new ArrayList<MethodReturnValueHandler>(dhxMethodProcessors);

    argumentResolvers.add(new MessageContextMethodArgumentResolver());

    return new DefaultMethodEndpointAdapter() {{
      setMethodArgumentResolvers(argumentResolvers);
      setMethodReturnValueHandlers(returnValueHandlers);
    }};
  }

  /**
   * Method returns bean List of MarshallingPayloadMethodProcessors.
   * 
   * @return bean List of MarshallingPayloadMethodProcessors
   */
  @Bean(name = "dhxMethodProcessors")
  public List<MarshallingPayloadMethodProcessor> methodProcessors() {
    List<MarshallingPayloadMethodProcessor> retVal =
        new ArrayList<MarshallingPayloadMethodProcessor>();
    Jaxb2Marshaller marshallerMtom = marshaller;
    retVal.add(new MarshallingPayloadMethodProcessor(marshallerMtom));

    return retVal;
  }

  /**
   * Defines WSDL.
   * 
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhx")
  public SimpleWsdl11Definition defaultWsdl11Definition() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFile());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(
        wsdlResource);
    return wsdlDef;
  }

}

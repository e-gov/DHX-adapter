package ee.ria.dhx.server.endpoint.config;


import ee.ria.dhx.server.config.DhxServerConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@ComponentScan(basePackages = "ee.ria.dhx.server.endpoint")
public class DhxServerEndpointConfig extends WsConfigurationSupport {

  @Autowired
  DhxServerConfig config;

  @Autowired
  @Qualifier("axiomSoapMessageFactoryReceive")
  Jaxb2Marshaller marshaller;

  /**
   * Injects DefaultMethodEndpointAdapter which supports SOAP message attachments. Sets proper
   * marshaller. That bean might iterfere with another same bean if it is defined(in that case most
   * probably code need to be changed to define single bean which will staisfy both needs).
   * 
   * @return DefaultMethodEndpointAdapter
   */
  @Bean(name = "dhxServerMethodEndpointAdapter")
  public DefaultMethodEndpointAdapter dhxServerMethodEndpointAdapter(
          DefaultMethodEndpointAdapter defaultMethodEndpointAdapter,
          List<MarshallingPayloadMethodProcessor> dhxServerMethodProcessors) {
    final List<MethodArgumentResolver> argumentResolvers = new ArrayList<MethodArgumentResolver>(dhxServerMethodProcessors);
    final List<MethodReturnValueHandler> returnValueHandlers = new ArrayList<MethodReturnValueHandler>(dhxServerMethodProcessors);

    argumentResolvers.add(new MessageContextMethodArgumentResolver());

    argumentResolvers.addAll(defaultMethodEndpointAdapter.getMethodArgumentResolvers());
    returnValueHandlers.addAll(defaultMethodEndpointAdapter.getMethodReturnValueHandlers());

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
  @Bean(name = "dhxServerMethodProcessors")
  public List<MarshallingPayloadMethodProcessor> methodProcessors() {
    return new ArrayList<MarshallingPayloadMethodProcessor>() {{
      add(new MarshallingPayloadMethodProcessor(marshaller));
    }};
  }


  /**
   * Defines WSDL v1.
   * 
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhlv1")
  public SimpleWsdl11Definition wsdl11Definitionv1() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFilev1());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }

  /**
   * Defines WSDL v2.
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhlv2")
  public SimpleWsdl11Definition wsdl11Definitionv2() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFilev2());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }

  /**
   * Defines WSDL v3.
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhlv3")
  public SimpleWsdl11Definition wsdl11Definitionv3() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFilev3());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }

  /**
   * Defines WSDL v4.
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhlv4")
  public SimpleWsdl11Definition wsdl11Definitionv4() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFilev4());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }


}

package ee.bpw.dhx.server.endpoint.config;


import ee.bpw.dhx.ws.config.DhxConfig;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
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
@Getter
@Configuration
@ComponentScan(basePackages = "ee.bpw.dhx.server.endpoint")
@Slf4j
public class DhxServerEndpointConfig extends WsConfigurationSupport {

  @Autowired
  DhxConfig config;

 
  /**
   * Defines WSDL.
   * 
   * @return SimpleWsdl11Definition
   */
  @Bean(name = "dhxServer")
  public SimpleWsdl11Definition defaultWsdl11Definition() {
    Resource wsdlResource = new ClassPathResource(config.getWsdlFile());
    SimpleWsdl11Definition wsdlDef = new SimpleWsdl11Definition(wsdlResource);
    return wsdlDef;
  }
  
  /**
   * Injects DefaultMethodEndpointAdapter which supports SOAP message attachments. Sets proper
   * marshaller. That bean might iterfere with another same bean if it is defined(in that case most
   * probably code need to be changed to define single bean which will staisfy both needs).
   * 
   * @return DefaultMethodEndpointAdapter
   */
  @Bean(name="dhxServerMethodEndpointAdapter")
  public DefaultMethodEndpointAdapter dhxMethodEndpointAdapter() {
    List<MethodArgumentResolver> argumentResolvers = null;
    List<MethodReturnValueHandler> returnValueHandlers =
        null;
    if (argumentResolvers == null) {
      argumentResolvers = new ArrayList<MethodArgumentResolver>();
    }
    if (returnValueHandlers == null) {
      returnValueHandlers = new ArrayList<MethodReturnValueHandler>();
    }
    returnValueHandlers.addAll(methodProcessors());
    argumentResolvers.addAll(methodProcessors());
    argumentResolvers.add(new MessageContextMethodArgumentResolver());
    DefaultMethodEndpointAdapter adapter = new DefaultMethodEndpointAdapter();
    adapter.setMethodArgumentResolvers(argumentResolvers);
    adapter.setMethodReturnValueHandlers(returnValueHandlers);
    return adapter;
  }


  /**
   * Method returns bean List of MarshallingPayloadMethodProcessors.
   * 
   * @return bean List of MarshallingPayloadMethodProcessors
   */
  @Bean(name="dhxServerMethodProcessors")
  public List<MarshallingPayloadMethodProcessor> methodProcessors() {
    List<MarshallingPayloadMethodProcessor> retVal =
        new ArrayList<MarshallingPayloadMethodProcessor>();
    Jaxb2Marshaller dhxJaxb2Marshaller = new Jaxb2Marshaller();
    dhxJaxb2Marshaller.setMtomEnabled(true);
    log.debug("Creating marshaller for folowing paths: " + "");
    String[] context = {"ee.riik.xrd.dhl.producers.producer.dhl"};
    dhxJaxb2Marshaller.setContextPaths(context);
    Jaxb2Marshaller marshallerMtom = config.getDhxJaxb2Marshaller();
    retVal.add(new MarshallingPayloadMethodProcessor(marshallerMtom));

    return retVal;
  }
  

}

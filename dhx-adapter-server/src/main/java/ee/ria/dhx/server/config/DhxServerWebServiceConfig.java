package ee.ria.dhx.server.config;



import ee.ria.dhx.server.endpoint.config.DhxServerEndpointConfig;
import ee.ria.dhx.ws.config.endpoint.DhxEndpointConfig;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.util.HashMap;
import java.util.Map;

import javax.xml.soap.SOAPMessage;

/**
 * Class creates beans needed for web services. Those beans are meant to use only if there is no
 * other web services in application. Otherwise those beans might interfere with the ones already
 * defined.
 *
 * @author Aleksei Kokarev
 *
 */
@Configuration
public class DhxServerWebServiceConfig {


  /**
   * Sets servlet registration bean. Registers web services on configured path
   *
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean(name = "dhxServlet")
  public ServletRegistrationBean dhxMessageDispatcherServlet(ApplicationContext applicationContext) {
    AnnotationConfigWebApplicationContext applicationAnnotationContext = new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxEndpointConfig.class);

    MessageDispatcherServlet messageDispatcherServlet =
            messageDispatcherServlet(applicationAnnotationContext, "axiomSoapMessageFactoryReceive");

    return new ServletRegistrationBean(messageDispatcherServlet, "/" + "ws" + "/*") {{
      setName("dhx");
    }};
  }

  /**
   * Sets servlet registration bean. Registers web services on configured path
   *
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean(name = "dhxServerServlet")
  public ServletRegistrationBean dhxServerMessageDispatcherServlet(ApplicationContext applicationContext) {
    AnnotationConfigWebApplicationContext applicationAnnotationContext = new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxServerEndpointConfig.class);

    MessageDispatcherServlet messageDispatcherServlet =
            messageDispatcherServlet(applicationAnnotationContext, "axiomSoapMessageFactorySend");

    return new ServletRegistrationBean(messageDispatcherServlet, "/" + "wsServer" + "/*") {{
      setName("dhxServer");
    }};
  }

  public MessageDispatcherServlet messageDispatcherServlet(final ApplicationContext applicationContext,
                                                           final String messageFactoryBeanName) {
    return new MessageDispatcherServlet() {{
      setTransformWsdlLocations(true);
      setApplicationContext(applicationContext);
      setMessageFactoryBeanName(messageFactoryBeanName);
    }};
  }

  /**
   * Creates messageFactory for receiving and returns.
   * @return messagefactory
   */
  @Bean
  public SoapMessageFactory axiomSoapMessageFactoryReceive() {
    return new AxiomSoapMessageFactory() {{
      setAttachmentCaching(true);
      setAttachmentCacheThreshold(1024 /* MB */ * 1024 /* KB */ * 1024 /* Byte */); // TODO: make it configurable
    }};
  }


  /**
   * Creates messageFactory for sending and returns.
   * @return messagefactory
   */
  @Bean
  public SoapMessageFactory axiomSoapMessageFactorySend() {
    return new AxiomSoapMessageFactory() {{
      setAttachmentCaching(true);
      setAttachmentCacheThreshold(10 /* MB */ * 1024 /* KB */ * 1024 /* Byte */); // TODO: make it configurable
    }};
  }


}

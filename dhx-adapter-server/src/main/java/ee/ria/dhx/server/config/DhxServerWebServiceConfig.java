package ee.ria.dhx.server.config;



import ee.ria.dhx.server.endpoint.config.DhxServerEndpointConfig;
import ee.ria.dhx.ws.config.DhxWebServiceConfig;
import ee.ria.dhx.ws.config.endpoint.DhxEndpointConfig;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

import java.io.File;
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

  @Value("${soap.dhx.attachment.cache.threshold:#{null}}")
  @Getter
  Integer attachmentCacheThreshold;

  @Value("${soap.dhx.attachment.cache.dir:#{null}}")
  @Getter
  String attachmentCacheDir;


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

  public MessageDispatcherServlet messageDispatcherServlet(final WebApplicationContext applicationContext,
                                                           final String messageFactoryBeanName) {
    return new MessageDispatcherServlet(applicationContext) {{
      setTransformWsdlLocations(true);
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
      Integer attachmentCacheThreshold = DhxServerWebServiceConfig.this.getAttachmentCacheThreshold();
      if (attachmentCacheThreshold != null) {
        setAttachmentCaching(true);
        setAttachmentCacheThreshold(attachmentCacheThreshold /* MB */ * 1024 /* KB */ * 1024 /* Byte */);

        String attachmentCacheDir = DhxServerWebServiceConfig.this.getAttachmentCacheDir();
        if (attachmentCacheDir != null) {
          setAttachmentCacheDir(new File(attachmentCacheDir));
        }
      }
    }};
  }


}

package ee.ria.dhx.server.config;



import ee.ria.dhx.server.endpoint.config.DhxServerEndpointConfig;
import ee.ria.dhx.ws.config.endpoint.DhxEndpointConfig;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
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
  public ServletRegistrationBean dhxMessageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    AnnotationConfigWebApplicationContext applicationAnnotationContext =
        new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxEndpointConfig.class);
    servlet.setApplicationContext(applicationAnnotationContext);
    servlet.setTransformWsdlLocations(true);
    servlet.setMessageFactoryBeanName("messageFactory");
    ServletRegistrationBean servletBean = new ServletRegistrationBean(servlet, "/" + "ws" + "/*");
    servletBean.setName("dhx");
    return servletBean;
  }

  /**
   * Creates messageFactory and returns.
   * @return messagefactory
   */
  @Bean(name = "messageFactory")
  public SaajSoapMessageFactory messageFactory() {
    SaajSoapMessageFactory smf = new SaajSoapMessageFactory();
    Map<String, String> props = new HashMap<String, String>();
    props.put(SOAPMessage.WRITE_XML_DECLARATION, Boolean.toString(true));
    smf.setMessageProperties(props);
    return smf;
  }

  /**
   * Sets servlet registration bean. Registers web services on configured path
   * 
   * @param applicationContext - context of the application
   * @return ServletRegistrationBean
   */
  @Bean(name = "dhxServerServlet")
  public ServletRegistrationBean dhxServerMessageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    AnnotationConfigWebApplicationContext applicationAnnotationContext =
        new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxServerEndpointConfig.class);
    servlet.setApplicationContext(applicationAnnotationContext);
    servlet.setTransformWsdlLocations(true);
    ServletRegistrationBean servletBean =
        new ServletRegistrationBean(servlet, "/" + "wsServer" + "/*");
    servletBean.setName("dhxServer");
    return servletBean;
  }


}

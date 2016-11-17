package ee.ria.dhx.server.config;






import ee.ria.dhx.server.endpoint.config.DhxServerEndpointConfig;
import ee.ria.dhx.ws.beanconfig.DhxEndpointConfig;
import ee.ria.dhx.ws.config.DhxConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;

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
   // servlet.setApplicationContext(applicationContext);
    AnnotationConfigWebApplicationContext applicationAnnotationContext = new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxEndpointConfig.class);
    servlet.setApplicationContext(applicationAnnotationContext);
    servlet.setTransformWsdlLocations(true);
    servlet.setContextConfigLocation("ee.bpw.dhx.ws.beanconfig.DhxEndpointConfig");
    ServletRegistrationBean servletBean =  new ServletRegistrationBean(servlet, "/" + "ws" + "/*");
    servletBean.setName("dhx");
    return servletBean;
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
    //servlet.setApplicationContext(applicationContext);
    AnnotationConfigWebApplicationContext applicationAnnotationContext = new AnnotationConfigWebApplicationContext();
    applicationAnnotationContext.setParent(applicationContext);
    applicationAnnotationContext.register(DhxServerEndpointConfig.class);
    servlet.setApplicationContext(applicationAnnotationContext);
    servlet.setTransformWsdlLocations(true);
    servlet.setContextConfigLocation("ee.bpw.dhx.server.endpoint.config.DhxServerEndpointConfig");
    ServletRegistrationBean servletBean = new ServletRegistrationBean(servlet, "/" + "wsServer" + "/*");
    servletBean.setName("dhxServer");
    return servletBean;
  }


}

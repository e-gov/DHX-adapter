package ee.ria.dhx.ws.config.marshaller;

import ee.ria.dhx.ws.config.DhxConfig;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

/**
 * Contains beans needed for marshalling.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Configuration
public class MarshallerConfig {

  @Autowired
  DhxConfig dhxConfig;

  /**
   * initializes using configured marshall context if needed and returns Jaxb2Marshaller.
   * 
   * @return Jaxb2Marshaller
   */
  @Bean(name = "dhxJaxb2Marshaller")
  public Jaxb2Marshaller getDhxJaxb2Marshaller() {
    Jaxb2Marshaller dhxJaxb2Marshaller = null;
    dhxJaxb2Marshaller = new Jaxb2Marshaller();
    dhxJaxb2Marshaller.setMtomEnabled(true);
    log.debug("Creating marshaller for folowing paths: "
        + dhxConfig.getMarshallContext());
    dhxJaxb2Marshaller.setContextPaths(dhxConfig.getMarshallContextAsList());
    return dhxJaxb2Marshaller;
  }

}

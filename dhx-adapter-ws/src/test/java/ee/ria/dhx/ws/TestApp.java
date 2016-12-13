package ee.ria.dhx.ws;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.TestPropertySource;

// @SpringBootApplication
@TestPropertySource("classpath:test-application.properties")
@ComponentScan(basePackages = "ee.ria.dhx.ws.config,ee.ria.dhx.ws.config.endpoint,ee.ria.dhx.ws,ee.ria.dhx.ws.service.impl")
@Slf4j
// @EnableAutoConfiguration
public class TestApp {


  @Bean
  public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
    return new PropertySourcesPlaceholderConfigurer();
  }


  @Bean
  @Primary
  public Jaxb2Marshaller jaxb2MarshallerSpy(Jaxb2Marshaller dhxJaxb2Marshaller) {
    return Mockito.spy(dhxJaxb2Marshaller);
  }

  @Bean
  @Primary
  public AddressService addressServiceSpy(AddressService addressService) {
    return Mockito.spy(addressService);
  }


  @Bean
  public DhxImplementationSpecificService getDhxImplementationSpecificService()
      throws DhxException {
    DhxImplementationSpecificService specificService =
        Mockito.mock(DhxImplementationSpecificService.class);
    return specificService;
  }

}

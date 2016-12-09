package ee.ria.dhx.ws;

import ee.ria.dhx.exception.DhxException;

import ee.ria.dhx.types.InternalXroadMember;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.config.SoapConfig;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.DhxImplementationSpecificService;
import ee.ria.dhx.ws.service.DhxMarshallerService;
import ee.ria.dhx.ws.service.DhxPackageProviderService;
import ee.ria.dhx.ws.service.DhxPackageService;
import ee.ria.dhx.ws.service.impl.AddressServiceImpl;
import ee.ria.dhx.ws.service.impl.DhxGateway;
import ee.ria.dhx.ws.service.impl.DhxMarshallerServiceImpl;
import ee.ria.dhx.ws.service.impl.DhxPackageProviderServiceImpl;
import ee.ria.dhx.ws.service.impl.DhxPackageServiceImpl;
import ee.ria.dhx.ws.service.impl.ExampleDhxImplementationSpecificService;

import lombok.extern.slf4j.Slf4j;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

//@SpringBootApplication
@TestPropertySource("classpath:test-application.properties")
@ComponentScan(basePackages = "ee.ria.dhx.ws.config,ee.ria.dhx.ws.config.endpoint,ee.ria.dhx.ws,ee.ria.dhx.ws.service.impl")
@Slf4j
//@EnableAutoConfiguration
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
  public DhxImplementationSpecificService getDhxImplementationSpecificService() throws DhxException{
    DhxImplementationSpecificService specificService = Mockito.mock(DhxImplementationSpecificService.class);
    return specificService;
  }
    
}


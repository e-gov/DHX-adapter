package ee.ria.dhx.server;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.server.persistence.entity.Folder;
import ee.ria.dhx.server.persistence.repository.FolderRepository;
import ee.ria.dhx.server.service.ConvertationService;
import ee.ria.dhx.ws.service.AddressService;
import ee.ria.dhx.ws.service.impl.AddressServiceImplSpyProvider;

import lombok.extern.slf4j.Slf4j;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ws.soap.SoapMessageFactory;
import org.springframework.ws.soap.axiom.AxiomSoapMessageFactory;

import java.io.IOException;

// @SpringBootApplication
@TestPropertySource("classpath:test-application.properties")
@ComponentScan(basePackages = {
        "ee.ria.dhx.ws.config",
        "ee.ria.dhx.ws.schedule",
        "ee.ria.dhx.ws.service.impl",
        "ee.ria.dhx.server.service",
        "ee.ria.dhx.server.config",
        "ee.ria.dhx.server.persistence.*",
        "ee.ria.dhx.server.scheduler",
        "ee.ria.dhx.ws",
        "ee.ria.dhx.server.endpoint.config"
})
@EnableTransactionManagement
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
  public ConvertationService getConvertationService(ConvertationService convertationService) {
    return Mockito.spy(convertationService);
  }

  @Bean
  @Primary
  public AddressService addressServiceSpy(AddressService addressService)
      throws DhxException, IOException {
    return AddressServiceImplSpyProvider.getAddressServiceSpy(addressService);
  }

  @Bean
  @Primary
  public FolderRepository getFodlerRepository(FolderRepository repository) {
    Folder folder = new Folder();
    folder.setName("/");
    repository.save(folder);
    return repository;
  }

  @Bean
  @Primary
  public SoapMessageFactory messageFactory(SoapMessageFactory axiomSoapMessageFactoryReceive) {
    return Mockito.spy(axiomSoapMessageFactoryReceive);
  }

}

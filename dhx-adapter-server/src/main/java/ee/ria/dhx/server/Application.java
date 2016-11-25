package ee.ria.dhx.server;

import ee.ria.dhx.server.entity.Classificator;
import ee.ria.dhx.server.entity.Folder;
import ee.ria.dhx.server.entity.Organisation;
import ee.ria.dhx.server.repository.ClassificatorRepository;
import ee.ria.dhx.server.repository.FolderRepository;
import ee.ria.dhx.server.repository.OrganisationRepository;
import ee.ria.dhx.server.service.util.StatusEnum;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Class containing main method.
 * 
 * @author Aleksei Kokarev
 *
 */
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
@EnableWebMvc
@ComponentScan(basePackages = "ee.ria.dhx.ws.config,ee.ria.dhx.ws.schedule,"
    + "ee.ria.dhx.ws.service.impl,ee.ria.dhx.server.service,ee.ria.dhx.server.config"
    + ",ee.ria.dhx.server.repository,ee.ria.dhx.server.entity,ee.ria.dhx.server.scheduler")
@EnableAsync
@PropertySource("classpath:dhx-application.properties")
@Slf4j
public class Application extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(Application.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  public String demo(FolderRepository repository) {
    Folder folder = new Folder();
    folder.setName("/");
    repository.save(folder);
    /*
     * // save a couple of organisations Organisation org = new Organisation(1L, "1", "GOV",
     * "Org 1", "2.1", "DHX", true, null, null); repository.save(org); repository.save(new
     * Organisation(2L, "1", "COM", "Org 2", "2.1", "DHX.subsystem", true, null, null));
     * repository.save(new Organisation(3L, "3", "GOV2", "Org 3", "1.0", "DHX", true, null, null));
     * repository.save(new Organisation(4L, "4", "GOV", "Org 4", "2.1", "DHX", true, org, null));
     * repository.save(new Organisation(5L, "5", "GOV", "Org 5", "2.1", "subsystem", true, org,
     * null));
     * 
     * // fetch all customers log.info("Customers found with findAll():");
     * log.info("-------------------------------"); for (Organisation organisation :
     * repository.findAll()) { log.info(organisation.toString()); } log.info("");
     * 
     * // fetch an individual customer by ID Organisation organisation = repository.findOne(1L);
     * log.info("Customer found with findOne(1L):"); log.info("--------------------------------");
     * log.info(organisation.toString()); log.info("");
     * 
     * // fetch customers by last name log.info("Customer found with findByLastName('Bauer'):");
     * log.info("--------------------------------------------"); for (Organisation bauer :
     * repository.findByMemberCodeAndSubsystem("1", "DHX")) { log.info(bauer.toString()); }
     * log.info("");
     */
    return "ok";
  }
  

  @Bean
  public String demo2(ClassificatorRepository repository) {
    Classificator cla = new Classificator();
   cla.setName(StatusEnum.IN_PROCESS.getClassificatorName());
   cla.setKlassifikaatorId(101);
    repository.save(cla);
    
    cla = new Classificator();
    cla.setName(StatusEnum.FAILED.getClassificatorName());
    cla.setKlassifikaatorId(103);
     repository.save(cla);
     
     cla = new Classificator();
     cla.setName(StatusEnum.RECEIVED.getClassificatorName());
     cla.setKlassifikaatorId(102);
      repository.save(cla);
    /*
     * // save a couple of organisations Organisation org = new Organisation(1L, "1", "GOV",
     * "Org 1", "2.1", "DHX", true, null, null); repository.save(org); repository.save(new
     * Organisation(2L, "1", "COM", "Org 2", "2.1", "DHX.subsystem", true, null, null));
     * repository.save(new Organisation(3L, "3", "GOV2", "Org 3", "1.0", "DHX", true, null, null));
     * repository.save(new Organisation(4L, "4", "GOV", "Org 4", "2.1", "DHX", true, org, null));
     * repository.save(new Organisation(5L, "5", "GOV", "Org 5", "2.1", "subsystem", true, org,
     * null));
     * 
     * // fetch all customers log.info("Customers found with findAll():");
     * log.info("-------------------------------"); for (Organisation organisation :
     * repository.findAll()) { log.info(organisation.toString()); } log.info("");
     * 
     * // fetch an individual customer by ID Organisation organisation = repository.findOne(1L);
     * log.info("Customer found with findOne(1L):"); log.info("--------------------------------");
     * log.info(organisation.toString()); log.info("");
     * 
     * // fetch customers by last name log.info("Customer found with findByLastName('Bauer'):");
     * log.info("--------------------------------------------"); for (Organisation bauer :
     * repository.findByMemberCodeAndSubsystem("1", "DHX")) { log.info(bauer.toString()); }
     * log.info("");
     */
    return "ok";
  }
  
}

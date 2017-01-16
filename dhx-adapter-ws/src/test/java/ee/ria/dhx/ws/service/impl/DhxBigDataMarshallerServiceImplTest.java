package ee.ria.dhx.ws.service.impl;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@Slf4j
public class DhxBigDataMarshallerServiceImplTest {


  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  DhxMarshallerService dhxMarshallerService;


  @Before
  public void init() throws DhxException, JAXBException {
    dhxMarshallerService = new DhxMarshallerServiceImpl();
    DhxConfig config = new DhxConfig();
    config.setMarshallContext("ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1"
        + ":ee.ria.dhx.types.eu.x_road.dhx.producer:ee.ria.dhx.types.eu.x_road.xsd.identifiers"
        + ":ee.ria.dhx.types.eu.x_road.xsd.representation:ee.ria.dhx.types.eu.x_road.xsd.xroad");
    ((DhxMarshallerServiceImpl) dhxMarshallerService).setConfig(config);
    CapsuleConfig capsuleConfig = new CapsuleConfig();
    capsuleConfig.setCurrentCapsuleVersion(CapsuleVersionEnum.V21);
    ((DhxMarshallerServiceImpl) dhxMarshallerService).setCapsuleConfig(capsuleConfig);
    ((DhxMarshallerServiceImpl) dhxMarshallerService).init();
  }

  @Test
  public void unmarshallAndValidate() throws DhxException, IOException {
    log.info(System.getProperty("java.io.tmpdir"));
    File testFile = testFolder.newFile("trying");
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DecContainer container =
        dhxMarshallerService.unmarshallAndValidate(new FileInputStream(file), null);
    File marshallerFile = dhxMarshallerService.marshall(container); 
    marshallerFile.delete();

  }


  /*
   * private String readFile(File file) throws IOException { BufferedReader reader = new
   * BufferedReader( new InputStreamReader( new FileInputStream(file), "UTF8")); String line = null;
   * StringBuilder stringBuilder = new StringBuilder(); String ls =
   * System.getProperty("line.separator");
   * 
   * try { while ((line = reader.readLine()) != null) { stringBuilder.append(line);
   * stringBuilder.append(ls); }
   * 
   * return stringBuilder.toString(); } finally { reader.close(); } }
   */

}

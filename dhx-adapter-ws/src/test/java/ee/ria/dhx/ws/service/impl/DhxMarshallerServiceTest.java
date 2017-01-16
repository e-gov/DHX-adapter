package ee.ria.dhx.ws.service.impl;

import static org.junit.Assert.assertEquals;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;
import ee.ria.dhx.util.CapsuleVersionEnum;
import ee.ria.dhx.ws.config.CapsuleConfig;
import ee.ria.dhx.ws.config.DhxConfig;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBException;

@Slf4j
public class DhxMarshallerServiceTest {


  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  DhxMarshallerService dhxMarshallerService;

  final String zipBase64Content =
      "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";

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
  public void unmarshallAndMarshall() throws DhxException, IOException {
    log.info(System.getProperty("java.io.tmpdir"));
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    DecContainer container =
        dhxMarshallerService.unmarshallAndValidate(new FileInputStream(file), null);
    File containerFile = container.getFile().get(0).getZipBase64Content();
    String zipContent = readFile(containerFile);
    assertEquals(zipBase64Content, zipContent.replaceAll("\\r|\\n", ""));
    File marshalledFile = dhxMarshallerService.marshall(container);
    String marshalledContent = readFile(marshalledFile);
    log.debug("marsh content: " + marshalledContent);
    log.debug("index1 " + marshalledContent.indexOf("ZipBase64Content>"));
    log.debug("index2 " + marshalledContent.indexOf("</"));
    Integer zipIndex = marshalledContent.indexOf("ZipBase64Content>") + 17;
    String marshalledZipContent =
        marshalledContent.substring(zipIndex, marshalledContent.indexOf("</", zipIndex));
    assertEquals(zipBase64Content, marshalledZipContent.replaceAll("\\r|\\n", ""));
    marshalledFile.delete();

  }

  private String readFile(File file) throws IOException {
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls =
        System.getProperty("line.separator");

    try {
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }

      return stringBuilder.toString();
    } finally {
      reader.close();
    }
  }


}

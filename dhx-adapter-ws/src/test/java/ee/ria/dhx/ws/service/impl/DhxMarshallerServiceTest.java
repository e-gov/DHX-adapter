package ee.ria.dhx.ws.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.types.ee.riik.schemas.deccontainer.vers_2_1.DecContainer;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;

public class DhxMarshallerServiceTest {

  @Mock
  Unmarshaller unmarshaller;

  @Mock
  Marshaller marshaller;

  DhxMarshallerServiceImpl dhxMarshallerService;

  @Before
  public void init() throws DhxException {
    MockitoAnnotations.initMocks(this);
    dhxMarshallerService = new DhxMarshallerServiceImpl();
    dhxMarshallerService = Mockito.spy(dhxMarshallerService);
    Mockito.doReturn(unmarshaller).when(dhxMarshallerService).getUnmarshaller();
    Mockito.doReturn(marshaller).when(dhxMarshallerService).getMarshaller();
  }

  @Ignore
  @Test
  public void marshallObject() throws DhxException, JAXBException {
    DecContainer container = new DecContainer();
    dhxMarshallerService.marshall(container);
    verify(marshaller, times(1)).marshal(eq(container), any(File.class));
  }


  @Test
  public void marshallToOutputStream() throws DhxException, JAXBException {
    DecContainer container = new DecContainer();
    OutputStream stream = null;
    dhxMarshallerService.marshallToOutputStream(container, stream);
    verify(marshaller, times(1)).marshal(eq(container), any(OutputStream.class));
  }

  @Ignore
  @Test
  public void marshallToOutputStreamNoNamespacePrefixes() throws DhxException, JAXBException {
    DecContainer container = new DecContainer();
    OutputStream stream = null;
    dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(container, stream);
    verify(marshaller, times(1)).marshal(eq(container), any(XMLStreamWriter.class));
  }

  @Test
  public void marshallToResult() throws DhxException, JAXBException {
    DecContainer container = new DecContainer();
    Result result = null;
    dhxMarshallerService.marshallToResult(container, result);
    verify(marshaller, times(1)).marshal(eq(container), any(Result.class));
  }

  @Test
  public void marshallToWriter() throws DhxException, JAXBException {
    DecContainer container = new DecContainer();
    dhxMarshallerService.marshallToWriter(container);
    verify(marshaller, times(1)).marshal(eq(container), any(Writer.class));
  }

  @Ignore
  @Test
  public void unmarshallFile() throws DhxException, JAXBException, IOException {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    dhxMarshallerService.unmarshall(file);
    verify(unmarshaller, times(1)).unmarshal(any(FileInputStream.class));
  }

  @Ignore
  @Test
  public void unmarshallInputStream() throws DhxException, JAXBException, IOException {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    FileInputStream is = new FileInputStream(file);
    dhxMarshallerService.unmarshall(is);
    verify(unmarshaller, times(1)).unmarshal(eq(is));
  }

  @Test
  public void unmarshallSource() throws DhxException, JAXBException, IOException {
    Source source = null;
    dhxMarshallerService.unmarshall(source);
    verify(unmarshaller, times(1)).unmarshal(eq(source));
  }

  @Ignore
  @Test
  public void unmarshallFileInpusTream() throws DhxException, JAXBException, IOException {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    File fileSchema = new ClassPathResource("Dvk_kapsel_vers_2_1_eng_est.xsd").getFile();
    dhxMarshallerService.unmarshallAndValidate(file, new FileInputStream(fileSchema));
    verify(unmarshaller, times(1)).setSchema(any(Schema.class));
    verify(unmarshaller, times(1)).unmarshal(any(FileInputStream.class));
  }

  @Ignore
  @Test
  public void unmarshallInpusTreamInpusTream() throws DhxException, JAXBException, IOException {
    File file = new ClassPathResource("kapsel_21.xml").getFile();
    File fileSchema = new ClassPathResource("Dvk_kapsel_vers_2_1_eng_est.xsd").getFile();
    dhxMarshallerService.unmarshallAndValidate(new FileInputStream(file), new FileInputStream(
        fileSchema));
    verify(unmarshaller, times(1)).setSchema(any(Schema.class));
    verify(unmarshaller, times(1)).unmarshal(any(FileInputStream.class));
  }

}

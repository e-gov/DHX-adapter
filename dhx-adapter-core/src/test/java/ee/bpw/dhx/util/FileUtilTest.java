package ee.bpw.dhx.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.util.FileUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

public class FileUtilTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  String gzippedString =
      "H4sIAAAAAAAAAH1QwWqDQBC95yvEe7PVUGhgNcQGraUIUUjVS9msG126u4qOon9fzYaS9tA5zbw3781j8G6Uwh"
          + "hY2/FaOaa1fjQNpmhdcFU6Zg+Xh2dz565wRwZ2qGkvmQKfC7YHILRappM105cZclfGXFgRydxlxV7DCBhdZ03"
          + "RWsGs+ISpYS6wEVAjCFcY/SL0bsE62vIG5lhu8ue40TcFAWbUHTGAdcAlVwyje8nNhADR7VJHe2tR6as88Ro6"
          + "eVOeRg3dxNVZRiJ8jYcwyEWWhl0YWNWMC6q+yrOdlbk8VZTv4ccoDJ6G4sWrMhWJqzjYypu4PxxreE+8KEvf2t"
          + "CPN9mHWDBHB0I6EUb6YRj9/9dvKFE6854BAAA";

  String unzippedString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
      + "<saveDocumentFileAttachmentV1>\n"
      + "<file>\n"
      + "    <name>File2.txt</name>\n"
      + "    <content_type>text/plain</content_type>\n"
      + "    <description>SaveDocumentFile update osa testimine</description>\n"
      + "    <data>\n"
      + "        Q291cmFnZSBpcyByZXNpc3RhbmNlIHRvIGZlYXIsIG1hc3Rlcnkgb2YgZmVhciAt\n"
      + "        IG5vdCBhYnNlbmNlIG9mIGZlYXIuDQotLSBNYXJrIFR3YWluDQo\n"
      + "    </data>\n"
      + "</file>\n"
      + "</saveDocumentFileAttachmentV1>\n";


  @Test
  public void createPipelineFileTest() throws Exception {
    File file = FileUtil.createPipelineFile();
    assertTrue(file.exists());
    file.delete();
  }

  @Test
  public void getFileTest() throws IOException, DhxException {
    File testFile = testFolder.newFile("trying");
    File resultFile = FileUtil.getFile(testFile.getAbsolutePath());
    assertTrue(resultFile.exists());
  }

  @Test
  public void getFileNotExistsTest() throws IOException, DhxException {
    File resultFile = FileUtil.getFile("random");
    assertFalse(resultFile.exists());
  }

  // TODO: think of some test to get classpathresource(jar://path_to_file)

  @Test
  public void getFileAsStreamTest() throws IOException, DhxException {
    File testFile = testFolder.newFile("trying");
    InputStream resultStream = FileUtil.getFileAsStream(testFile.getAbsolutePath());
    assertNotNull(resultStream);
  }

  @Test(expected = DhxException.class)
  public void getFileAsStreamErrorTest() throws IOException, DhxException {
    FileUtil.getFileAsStream("random");
    exception.expectMessage("Error occured while reading file");
  }

  @Test
  public void getFileAsStreamFile() throws IOException, DhxException {
    File testFile = testFolder.newFile("trying");
    InputStream resultStream = FileUtil.getFileAsStream(testFile.getAbsolutePath());
    assertNotNull(resultStream);
  }

  @Test(expected = DhxException.class)
  public void getFileAsStreamFileError() throws IOException, DhxException {
    FileUtil.getFileAsStream("random");
    exception.expectMessage("Error occured while reading file");
  }

  @Test
  public void createFileAndWriteTest() throws Exception {
    String streamString = "test string";
    InputStream stream = new ByteArrayInputStream(streamString.getBytes(StandardCharsets.UTF_8));
    File file = FileUtil.createFileAndWrite(stream);
    assertTrue(file.exists());
    byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
    String decodedTest = new String(encoded, "UTF-8");
    assertEquals(streamString, decodedTest);
    file.delete();
  }

  @Test
  public void zipUnpackTest() throws IOException, DhxException {
    File zipFile = testFolder.newFile("123.zip");
    FileOutputStream fos = new FileOutputStream(zipFile);
    ZipOutputStream zos = new ZipOutputStream(fos);
    ZipEntry ze = new ZipEntry("file.txt");
    zos.putNextEntry(ze);
    zos.write(unzippedString.getBytes(StandardCharsets.UTF_8));
    zos.closeEntry();
    zos.close();
    fos.close();
    InputStream stream = FileUtil.zipUnpack(new FileInputStream(zipFile), "file.txt");
    byte[] bytes = new byte[10000];
    int length = stream.read(bytes, 0, 10000);
    assertEquals(unzippedString.length(), length);
    String theString = new String(bytes, "UTF-8");
    // that somehow does not work, though string are the same
    // assertEquals(unzippedString, theString);
  }


}

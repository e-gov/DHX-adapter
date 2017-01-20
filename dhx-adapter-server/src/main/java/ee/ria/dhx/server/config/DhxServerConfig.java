package ee.ria.dhx.server.config;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Server config.
 * @author Aleksei Kokarev
 *
 */
@Configuration
@Slf4j
public class DhxServerConfig {

  @Value("${documents.folder}")
  @Setter
  String documentsFolder;

  @Value("${dhx.server-wsdl-file:dhl.wsdl}")
  private String wsdlFile;


/**
 * Creates document in configured folder.
 * @return created file
 * @throws DhxException thrown if error occurs
 */
  @Loggable
  public File createDocumentFile() throws DhxException {
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("YYYY_MM_dd_HHmmss");
      Date date = new Date();
      String fileName = documentsFolder + "dhx_" + sdf.format(date) + UUID.randomUUID();
      log.debug("creating file: " + fileName);
      File file = new File(fileName);
      file.createNewFile();
      return file;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating new file. " + ex.getMessage(), ex);
    }
  }

  /**
   * Returns document found in configured folder.
   * @param fileName name of the file to find
   * @return found file
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public File getDocumentFile(String fileName) throws DhxException {
    File file = new File(documentsFolder + fileName);
    if (file.exists()) {
      return file;
    } else {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "File not found! file:" + documentsFolder + fileName);
    }
  }

  /**
   * by default dhl.wsdl.
   * 
   * @return the name of the wsdl file in classpath.
   */
  public String getWsdlFile() {
    return wsdlFile;
  }

  /**
   * by default dhl.wsdl.
   * 
   * @param wsdlFile the name of the wsdl file in classpath to set.
   */
  public void setWsdlFile(String wsdlFile) {
    this.wsdlFile = wsdlFile;
  }

}

package ee.ria.dhx.util;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

/**
 * Extension of {@link DataHandler} which allows to delete the underlying file. Caution! file can be
 * deleted only after datahandler is not needed anymore.
 * 
 * @author Aleksei Kokarev
 *
 */
public class FileDataHandler extends DataHandler {

  private FileDataSource fileDataSource;

  /**
   * Contructor.
   * 
   * @param fileDataSource filedatasource to create datahandler from
   */
  public FileDataHandler(FileDataSource fileDataSource) {
    super(fileDataSource);
    this.fileDataSource = fileDataSource;
  }

  /**
   * method deletes underlying file.
   */
  public void deleteFile() {
    fileDataSource.getFile().delete();
  }

}

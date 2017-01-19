package ee.ria.dhx.util;

import java.io.File;

import javax.activation.FileTypeMap;

public class StaticFileTypeMap extends FileTypeMap {

  private String staticContentType;

  public StaticFileTypeMap(String staticContentType) {
    this.staticContentType = staticContentType;
  }

  @Override
  public String getContentType(File file) {
    return staticContentType;
  }

  @Override
  public String getContentType(String filename) {
    return staticContentType;
  }

}

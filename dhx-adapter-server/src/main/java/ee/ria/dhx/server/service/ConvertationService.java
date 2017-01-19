package ee.ria.dhx.server.service;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.service.util.WsUtil;
import ee.ria.dhx.util.FileUtil;
import ee.ria.dhx.ws.service.DhxMarshallerService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;

/**
 * Class that converts Objects.
 * 
 * @author Aleksei Kokarev
 *
 */

@Slf4j
@Service
public class ConvertationService {

  @Autowired
  DhxMarshallerService dhxMarshallerService;

  private static final String DEFAULT_CONTENT_TYPE =
      " {http://www.w3.org/2001/XMLSchema}base64Binary";

  /**
   * Creates dataHandler from object. Object will be marshalled, GZipped, base64 encoded and written
   * to file. Datahandler will be created from that file.
   * 
   * @param obj - object to create dataHandler for
   * @return created {@link DataHandler}
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public DataHandler createDatahandlerFromObject(Object obj) throws DhxException {
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = WsUtil.getBase64EncodeStream(fos);
      zippedStream = WsUtil.getGZipCompressStream(base64Stream);
      dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(obj, zippedStream);
      zippedStream.finish();
      zippedStream.flush();
      zippedStream.close();
      zippedStream = null;
      base64Stream.flush();
      base64Stream.close();
      base64Stream = null;
      fos.flush();
      fos.close();
      fos = null;
      return FileUtil.getDatahandlerFromFile(file, DEFAULT_CONTENT_TYPE);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }
  }

  /**
   * Creates dataHandler from list of objects. Objects will be marshalled, GZipped, base64 encoded
   * and written to file. Datahandler will be created from that file. All objects are written to
   * same file one after another.
   * 
   * @param objList - list of objects to create dataHandler for
   * @return created {@link DataHandler}
   * @throws DhxException thrown if error occurs
   */
  public DataHandler createDatahandlerFromList(List<? extends Object> objList)
      throws DhxException {
    FileOutputStream fos = null;
    GZIPOutputStream zippedStream = null;
    OutputStream base64Stream = null;
    try {
      File file = FileUtil.createPipelineFile();
      fos = new FileOutputStream(file);
      base64Stream = WsUtil.getBase64EncodeStream(fos);
      zippedStream = WsUtil.getGZipCompressStream(base64Stream);
      for (Object obj : objList) {
        dhxMarshallerService.marshallToOutputStreamNoNamespacePrefixes(obj, zippedStream);
      }
      zippedStream.finish();
      zippedStream.flush();
      zippedStream.close();
      zippedStream = null;
      base64Stream.flush();
      base64Stream.close();
      base64Stream = null;
      fos.flush();
      fos.close();
      fos = null;
      return FileUtil.getDatahandlerFromFile(file, DEFAULT_CONTENT_TYPE);
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.FILE_ERROR,
          "Error occured while creating attachment for response. " + ex.getMessage(), ex);
    } finally {
      FileUtil.safeCloseStream(base64Stream);
      FileUtil.safeCloseStream(zippedStream);
      FileUtil.safeCloseStream(fos);
    }
  }
}

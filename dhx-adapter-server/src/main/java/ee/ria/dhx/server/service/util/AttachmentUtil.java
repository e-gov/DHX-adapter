package ee.ria.dhx.server.service.util;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.MessageConstraintException;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.mail.MessagingException;

@Slf4j
public class AttachmentUtil {
  
  public static InputStream gZipDecompress (InputStream stream) throws DhxException{
    try {
      GZIPInputStream gzis = new GZIPInputStream(stream);
      return gzis;
    } catch(IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Error occured whle unzipping file. " + ex.getMessage(), ex);
    }
  }
  
  public static GZIPOutputStream getGZipCompressStream (OutputStream stream) throws DhxException{
    try {
      GZIPOutputStream gzis = new GZIPOutputStream(stream);
      return gzis;
    } catch(IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR, "Error occured whle unzipping file. " + ex.getMessage(), ex);
    }
  }

  public static InputStream base64Decode (InputStream stream) throws DhxException{
    InputStream base64DecoderStream = Base64.getDecoder().wrap(stream);
    return base64DecoderStream;
  }
  
  public static OutputStream getBase64EncodeStream (OutputStream stream) throws DhxException{
    OutputStream base64EncoderStream = Base64.getEncoder().wrap(stream);
    return base64EncoderStream;
  }
  
  public static InputStream base64decodeAndUnzip (InputStream stream) throws DhxException{
    InputStream decoded = stream;
    try {
      decoded = base64Decode(decoded);
    }catch(DhxException ex) {
      log.info("Error occured while creating base64 decoded stream, maybe input is not base64 encoded, continue. " + ex.getMessage());
    }
    return gZipDecompress(decoded);
  }
  

  
  public static String readInput(InputStream fileStream) {
    StringBuffer buffer = new StringBuffer();
    try {
      // FileInputStream fis = new FileInputStream(filename);
      InputStreamReader isr = new InputStreamReader(fileStream, "UTF8");
      Reader in = new BufferedReader(isr);
      int ch;
      while ((ch = in.read()) > -1) {
        buffer.append((char) ch);
      }
      in.close();

      FileUtil.safeCloseReader(isr);
      return buffer.toString();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}

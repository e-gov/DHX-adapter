package ee.ria.dhx.server.service.util;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Utility methods for web services and attachments.
 * 
 * @author Aleksei Kokarev
 *
 */
@Slf4j
public class WsUtil {

  /**
   * Creates InputStream that will GZIP decompress the stream from input.
   * 
   * @param stream stream to decompress
   * @return decompressed stream
   * @throws DhxException thrown if error occurs
   */
  public static InputStream gzipDecompress(InputStream stream) throws DhxException {
    try {
      GZIPInputStream gzis = new GZIPInputStream(stream);
      return gzis;
    } catch (IOException ex) {
      //if we got not base64, then throw specific error about it
      if(ex.getMessage().indexOf("Illegal base64")>=0) {
        throw new DhxException(DhxExceptionEnum.EXTRACTION_ERROR,
          "Not a base64 stream! " + ex.getMessage(), ex);
      }
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured whle unzipping file. " + ex.getMessage(), ex);
    }
  }

  /**
   * Creates OutputStream that will GZIP compress the stream from input.
   * 
   * @param stream - stream to compress
   * @return compressed stream
   * @throws DhxException thrown if error occurs
   */
  public static GZIPOutputStream getGZipCompressStream(OutputStream stream) throws DhxException {
    try {
      GZIPOutputStream gzis = new GZIPOutputStream(stream);
      return gzis;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured whle unzipping file. " + ex.getMessage(), ex);
    }
  }

  /**
   * Creates InputStream that will BASE64 decode the stream from input.
   * 
   * @param stream - stream to decode
   * @return decoded stream
   * @throws DhxException thrown if error occurs
   */
  public static InputStream base64Decode(InputStream stream) throws DhxException {
    InputStream base64DecoderStream = Base64.getDecoder().wrap(stream);
    return base64DecoderStream;
  }


  /**
   * Creates OutputStream that will BASE64 encode the stream from input.
   * 
   * @param stream stream to encode
   * @return encoded stream
   * @throws DhxException thrown if error occurs
   */
  public static OutputStream getBase64EncodeStream(OutputStream stream) throws DhxException {
    OutputStream base64EncoderStream = Base64.getEncoder().wrap(stream);
    return base64EncoderStream;
  }

  /**
   * Creates InputStream that will BASE64 decode the stream from input.
   * 
   * @param stream stream to decode
   * @return decoded stream
   * @throws DhxException thrown if error occurs
   */
  public static InputStream base64decodeAndUnzip(InputStream stream) throws DhxException {
    InputStream decoded = stream;
    decoded = base64Decode(decoded);
    return gzipDecompress(decoded);
  }



  /**
   * Method reads inputstream into string.
   * 
   * @param fileStream stream to read
   * @return read String from {@link InputStream}
   */
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
    } catch (IOException ex) {
      ex.printStackTrace();
      return null;
    }
  }


  /**
   * Method parses and creates xml dom Document from inputstream.
   * 
   * @param objectStream - stream of the xml to parse
   * @return - dom document created from stream
   * @throws DhxException thrown if error occurs
   */
  public static Document xmlDocumentFromStream(InputStream objectStream)
      throws DhxException {
    try {
      DocumentBuilderFactory xmlFact = DocumentBuilderFactory.newInstance();
      xmlFact.setValidating(false);
      xmlFact.setNamespaceAware(false);
      xmlFact.setIgnoringElementContentWhitespace(true);
      DocumentBuilder builder = xmlFact.newDocumentBuilder();
      FileInputStream inStream = null;
      InputStreamReader inReader = null;
      org.w3c.dom.Document result = null;
      try {
        inReader = new InputStreamReader(objectStream, "UTF-8");
        InputSource src = new InputSource(inReader);
        result = builder.parse(src);
      } finally {
        FileUtil.safeCloseReader(inReader);
        FileUtil.safeCloseStream(inStream);
        inReader = null;
        inStream = null;
      }
      return result;
    } catch (IOException | SAXException | ParserConfigurationException ex) {
      throw new DhxException("Error occured while parsing XML. " + ex.getMessage(), ex);
    }
  }

}

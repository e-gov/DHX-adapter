package ee.ria.dhx.server.service.util;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.server.types.ee.riik.xrd.dhl.producers.producer.dhl.GetSendStatusV2ResponseTypeUnencoded;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Base64;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
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
   * @param stream - stream to decompress
   * @return - decompressed stream
   * @throws DhxException
   */
  public static InputStream gZipDecompress(InputStream stream) throws DhxException {
    try {
      GZIPInputStream gzis = new GZIPInputStream(stream);
      return gzis;
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured whle unzipping file. " + ex.getMessage(), ex);
    }
  }

  /**
   * Creates OutputStream that will GZIP compress the stream from input.
   * @param stream - stream to compress
   * @return - compressed stream
   * @throws DhxException
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
   * @param stream - stream to decode
   * @return - decoded stream
   * @throws DhxException
   */
  public static InputStream base64Decode(InputStream stream) throws DhxException {
    InputStream base64DecoderStream = Base64.getDecoder().wrap(stream);
    return base64DecoderStream;
  }

  
  /**
   * Creates OutputStream that will BASE64 encode the stream from input.
   * @param stream - stream to encode
   * @return - encoded stream
   * @throws DhxException
   */
  public static OutputStream getBase64EncodeStream(OutputStream stream) throws DhxException {
    OutputStream base64EncoderStream = Base64.getEncoder().wrap(stream);
    return base64EncoderStream;
  }

  /**
   * Creates InputStream that will BASE64 decode the stream from input.
   * @param stream - stream to decode
   * @return - decoded stream
   * @throws DhxException
   */
  public static InputStream base64decodeAndUnzip(InputStream stream) throws DhxException {
    InputStream decoded = stream;
    try {
      decoded = base64Decode(decoded);
    } catch (DhxException ex) {
      log.info("Error occured while creating base64 decoded stream, maybe input is not base64 encoded, continue. "
          + ex.getMessage());
    }
    return gZipDecompress(decoded);
  }



  /**
   * Method reads inputstream into string.
   * @param fileStream - stream to read
   * @return
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
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Creates XMLGregorianCalendar object from Date
   * @param date - date to create XMLGregorianCalendar from
   * @return - created XMLGregorianCalendar object
   * @throws DhxException
   */
  public static XMLGregorianCalendar getXmlGregorianCalendarFromDate(Date date)
      throws DhxException {
    if (date == null) {
      return null;
    }
    try {
      GregorianCalendar c = new GregorianCalendar();
      c.setTime(date);
      XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
      return date2;
    } catch (DatatypeConfigurationException ex) {
      throw new DhxException("Error occured while converting date. " + ex.getMessage(), ex);
    }
  }

  /**
   * Method parses and creates xml dom Document from inputstream.
   * @param objectStream - stream of the xml to parse
   * @return - dom document created from stream
   * @throws DhxException
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

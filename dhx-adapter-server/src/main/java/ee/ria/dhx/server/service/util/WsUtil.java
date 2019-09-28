package ee.ria.dhx.server.service.util;

import com.jcabi.aspects.Loggable;

import ee.ria.dhx.exception.DhxException;
import ee.ria.dhx.exception.DhxExceptionEnum;
import ee.ria.dhx.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

import org.springframework.ws.context.MessageContext;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.axiom.AxiomSoapMessage;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.activation.DataHandler;
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

  private static final String ATTACHMENT_CONTENT_ID_PREFIX = "cid:";

  /**
   * Creates InputStream that will GZIP decompress the stream from input.
   * 
   * @param stream stream to decompress
   * @param stremType Stream type
   * @return decompressed stream
   * @throws DhxException thrown if error occurs
   */
  public static InputStream gzipDecompress(InputStream stream, StreamTypeEnum stremType) throws DhxException {
    try {
      GZIPInputStream gzis = new GZIPInputStream(stream);
      return gzis;
    } catch (IOException ex) {
      
      switch (stremType) {
      
        case BASE64BASIC:
        case BASE64MIME:
          // if we got not base64, then throw specific error about it
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
   * Creates InputStream that will BASE64 mime decode the stream from input.
   * 
   * @param stream - stream to decode
   * @return decoded stream
   * @throws DhxException thrown if error occurs
   */
  public static InputStream base64MimeDecode(InputStream stream) throws DhxException {
    stream = ensureInputStreamSpeedForBase64(stream);
    InputStream base64DecoderStream = Base64.getMimeDecoder().wrap(stream);
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
    stream = ensureOutputStreamSpeedForBase64(stream);
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
  private static InputStream base64DecodeAndUnzip(InputStream stream) throws DhxException {
    return base64DecodeAndUnzip(stream, StreamTypeEnum.BASE64BASIC);
  }


  /**
   * Creates InputStream that will BASE64 Mime decode the stream from input.
   *
   * @param stream stream to decode
   * @return decoded stream
   * @throws DhxException thrown if error occurs
   */
  private static InputStream base64MimeDecodeAndUnzip(InputStream stream) throws DhxException {
    return base64DecodeAndUnzip(stream, StreamTypeEnum.BASE64MIME);
  }


  /**
   * Creates InputStream that will decode the stream from input.
   *
   * @param stream stream to decode
   * @return decoded stream
   * @throws DhxException thrown if error occurs
   */
  private static InputStream base64DecodeAndUnzip(InputStream stream, StreamTypeEnum streamType) throws DhxException {
    InputStream decoded = base64MimeDecode(stream);
    return gzipDecompress(decoded, streamType);
  }

  /**
   * PipedInputStream and FileInputStream do not play well with Base64 encoding/decoding.
   * Wrap these streams into BufferedInputStream to increase Base64 operations performance.
   *
   * @param stream stream to decode
   * @return
   */
  private static InputStream ensureInputStreamSpeedForBase64(InputStream stream) {
    return stream instanceof PipedInputStream || stream instanceof FileInputStream
            ? new BufferedInputStream(stream)
            : stream;
  }

  /**
   * PipedInputStream and FileInputStream do not play well with Base64 encoding/decoding.
   * Wrap these streams into BufferedInputStream to increase Base64 operations performance.
   *
   * @param stream stream to decode
   * @return
   */
  private static OutputStream ensureOutputStreamSpeedForBase64(OutputStream stream) {
    return stream instanceof PipedOutputStream || stream instanceof FileOutputStream
            ? new BufferedOutputStream(stream)
            : stream;
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

  /**
   * Method base64 decodes and unzips the handler's {@link InputStream}. If appears that handler's
   * {@link InputStream} is not base64 encoded, then is will be only unzipped.
   * 
   * @param handler handler which stream needs to be decoded and unzipped
   * @return decoded and unzipped {@link InputStream}
   * @throws DhxException thrown if error occurs
   */
  public static InputStream base64DecodeIfNeededAndUnzip(DataHandler handler)
      throws DhxException {
    InputStream unzippedStream = null;
    try {
      try {
        unzippedStream = WsUtil.base64DecodeAndUnzip(handler.getInputStream());
      } catch (DhxException ex) {
        // if input is not base64 or base64mime, then try to just unzip it. it might be if
        // Content-transfer-encoding is set to base64, then base64 is decoded automatically
        if (ex.getExceptionCode().equals(DhxExceptionEnum.EXTRACTION_ERROR)) {
      
          try {
            unzippedStream = WsUtil.base64MimeDecodeAndUnzip(handler.getInputStream()); 
          } catch (DhxException ex1) {    
        
            if (ex1.getExceptionCode().equals(DhxExceptionEnum.EXTRACTION_ERROR)) {
              log.debug(
                  "attachment appears to be not encoded in base64, "
                      + "trying to parse container without base64 decoding.");
              unzippedStream = WsUtil.gzipDecompress(handler.getInputStream(), StreamTypeEnum.GZIP);
            }
            else {
              throw ex1;
            }
          }
        } else {
          throw ex;
        }
      }
    } catch (IOException ex) {
      throw new DhxException(DhxExceptionEnum.TECHNICAL_ERROR,
          "Error occured while parsing attachment." + ex.getMessage(), ex);
    }
    return unzippedStream;
  }

  /**
   * Method finds and returns {@link DataHandler} of the attachment by attachment content id.
   * 
   * @param messageContext context to search for attachment
   * @param attachmentContentId content id of the attachment to find
   * @return {@link DataHandler} of the found attachment
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public static DataHandler extractAttachment(
      MessageContext messageContext, String attachmentContentId) throws DhxException {
    SoapMessage soapRequest = (SoapMessage) messageContext
        .getRequest();
    if (attachmentContentId.startsWith("cid:")) {
      attachmentContentId = attachmentContentId.substring(4);
    }
    log.debug("searching for attachment with content id: " + attachmentContentId);
    Attachment att = soapRequest.getAttachment(attachmentContentId);
    if (att == null) {
      att = soapRequest.getAttachment("<" + attachmentContentId + ">");
    }
    if (att == null) {
      try {
        String decoded = URLDecoder.decode(attachmentContentId, "UTF-8");
        att = soapRequest.getAttachment(decoded);
        if (att == null) {
          att = soapRequest.getAttachment("<" + decoded + ">");
        }
      } catch (UnsupportedEncodingException ex) {
        log.info("Error occured while URL decoding. " + ex.getMessage());
      }
    }
    if (att == null) {
      return null;
    }
    return att.getDataHandler();

  }

  /**
   * Method adds attachment to the context and returns generated content id of the attachment.
   * 
   * @param messageContext context to add attachment to
   * @param attachmentHandler {@link DataHandler} of the attachment to add
   * @return content id of the added attachment
   * @throws DhxException thrown if error occurs
   */
  @Loggable
  public static String addAttachment(
      MessageContext messageContext, DataHandler attachmentHandler) throws DhxException {
    String contentId = UUID.randomUUID().toString();

    AxiomSoapMessage soapResponse = (AxiomSoapMessage) messageContext.getResponse();
    soapResponse.addAttachment(contentId, attachmentHandler);

    return ATTACHMENT_CONTENT_ID_PREFIX + contentId;

  }

}

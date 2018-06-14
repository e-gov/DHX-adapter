package ee.ria.dhx.ws;

import org.apache.http.client.methods.HttpPost;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpComponentsConnection;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.http.HttpTransportConstants;

//import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;

//import ee.ria.dhx.ws.config.SoapConfig;

public class DhxHttpComponentsMessageSender extends HttpComponentsMessageSender {

  //@Autowired
  //SoapConfig soapConfig;
  
  @Override
  public WebServiceConnection createConnection(URI uri) throws IOException {
    
    HttpComponentsConnection connection = (HttpComponentsConnection) super.createConnection(uri);
    HttpPost postMethod = connection.getHttpPost();
    postMethod.addHeader(
        HttpTransportConstants.HEADER_CONTENT_TRANSFER_ENCODING,
        "base64");
    
    //if (!soapConfig.getKeepAlive()) {
      //postMethod.removeHeaders("Connection");
    //}
    
    return super.createConnection(uri);
  }

}

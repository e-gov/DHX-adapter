package ee.ria.dhx.ws;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpComponentsConnection;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.http.HttpTransportConstants;

import java.io.IOException;
import java.net.URI;

public class DhxHttpComponentsMessageSender extends HttpComponentsMessageSender {

  public DhxHttpComponentsMessageSender (HttpClient httpClient){
    super(httpClient);
  }

  @Override
  public WebServiceConnection createConnection(URI uri) throws IOException {
    HttpComponentsConnection connection = (HttpComponentsConnection) super.createConnection(uri);
    HttpPost postMethod = connection.getHttpPost();
    postMethod.addHeader(
        HttpTransportConstants.HEADER_CONTENT_TRANSFER_ENCODING,
        "base64");
    return super.createConnection(uri);
  }

}

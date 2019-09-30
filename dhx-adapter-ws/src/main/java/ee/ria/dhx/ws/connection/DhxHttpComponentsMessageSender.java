package ee.ria.dhx.ws.connection;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.protocol.HttpContext;
import org.springframework.ws.transport.WebServiceConnection;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.springframework.ws.transport.http.HttpTransportConstants;

import java.io.IOException;
import java.net.URI;

public class DhxHttpComponentsMessageSender extends HttpComponentsMessageSender {

    public DhxHttpComponentsMessageSender(HttpClient httpClient) {
        super(httpClient);
    }

    @Override
    public WebServiceConnection createConnection(URI uri) throws IOException {
        HttpPost httpPost = new HttpPost(uri);
        if (this.isAcceptGzipEncoding()) {
            httpPost.addHeader("Accept-Encoding", "gzip");
            httpPost.addHeader(HttpTransportConstants.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
        }

        HttpContext httpContext = this.createContext(uri);
        return new ChunkedHttpComponentsConnection(this.getHttpClient(), httpPost, httpContext);
    }

}

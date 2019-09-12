package ee.ria.dhx.ws.connection;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.protocol.HttpContext;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.http.HttpComponentsConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

public class ChunkedHttpComponentsConnection extends HttpComponentsConnection {

    private final HttpClient httpClient;
    private final HttpContext httpContext;

    private FileOutputStream requestOutputBuffer;
    private File tempFile;

    protected ChunkedHttpComponentsConnection(HttpClient httpClient, HttpPost httpPost, HttpContext httpContext) {
        super(httpClient, httpPost, httpContext);
        this.httpClient = httpClient;
        this.httpContext = httpContext;
    }

    @Override
    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        this.tempFile = File.createTempFile("dhx_chunked_", ".tmp");
        this.requestOutputBuffer = new FileOutputStream(tempFile);
    }

    @Override
    protected OutputStream getRequestOutputStream() throws IOException {
        return this.requestOutputBuffer;
    }

    @Override
    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        FileEntity chunkedEntity = new DeleteOnCloseChunkedFileEntity(this.tempFile);
        this.getHttpPost().setEntity(chunkedEntity);
        this.requestOutputBuffer = null;
        this.tempFile = null;
        if (this.httpContext != null) {
            setHttpResponse(this.httpClient.execute(this.getHttpPost(), this.httpContext));
        } else {
            setHttpResponse(this.httpClient.execute(this.getHttpPost()));
        }
    }

    @SneakyThrows
    private void setHttpResponse(HttpResponse httpResponse) {
        setParentField("httpResponse", httpResponse);
    }

    private void setParentField(String fieldName, Object newValue) throws NoSuchFieldException, IllegalAccessException {
        Field field = getClass().getSuperclass().getDeclaredField(fieldName);
        FieldUtils.writeField(field, this, newValue, true);
    }
}
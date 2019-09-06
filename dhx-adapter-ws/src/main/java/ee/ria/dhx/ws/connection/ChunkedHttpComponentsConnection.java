package ee.ria.dhx.ws.connection;

import ee.ria.dhx.ws.connection.stream.AsyncPipedOutputStream;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.transport.http.HttpComponentsConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.Executor;

public class ChunkedHttpComponentsConnection extends HttpComponentsConnection {

    private final HttpClient httpClient;
    private final HttpContext httpContext;
    private final Executor executor;

    private PipedOutputStream requestOutputBuffer;
    private PipedInputStream requestInputBuffer;

    protected ChunkedHttpComponentsConnection(HttpClient httpClient, HttpPost httpPost, HttpContext httpContext, Executor executor) {
        super(httpClient, httpPost, httpContext);
        this.httpClient = httpClient;
        this.httpContext = httpContext;
        this.executor = executor;
    }

    @Override
    protected void onSendBeforeWrite(WebServiceMessage message) throws IOException {
        final PipedOutputStream requestOutputBuffer;
        this.requestOutputBuffer = requestOutputBuffer = new AsyncPipedOutputStream(executor);
        this.requestInputBuffer = new PipedInputStream(requestOutputBuffer);
    }

    @Override
    protected OutputStream getRequestOutputStream() throws IOException {
        return this.requestOutputBuffer;
    }

    @Override
    protected void onSendAfterWrite(WebServiceMessage message) throws IOException {
        InputStreamEntity chunkedEntity = new InputStreamEntity(this.requestInputBuffer) {{
            setChunked(true);
        }};
        this.getHttpPost().setEntity(chunkedEntity);
        this.requestOutputBuffer = null;
        this.requestInputBuffer = null;
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
        Field field = super.getClass().getField(fieldName);
        FieldUtils.writeField(field, this, newValue, true);
    }
}
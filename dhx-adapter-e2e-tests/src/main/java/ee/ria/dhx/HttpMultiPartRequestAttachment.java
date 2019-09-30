package ee.ria.dhx;

import io.qameta.allure.attachment.AttachmentData;
import io.restassured.specification.MultiPartSpecification;

import java.util.List;
import java.util.Map;

public class HttpMultiPartRequestAttachment implements AttachmentData {
    private final String name;

    private final String url;

    private final String method;

    private final List<MultiPartSpecification> multiParts;

    private final Map<String, String> headers;


    public HttpMultiPartRequestAttachment(final String name, final String url, final String method,
                                          List<MultiPartSpecification> multiParts, final Map<String, String> headers
    ) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.multiParts = multiParts;
        this.headers = headers;
    }

    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public List<MultiPartSpecification> getMultiParts() {
        return multiParts;
    }

    @Override
    public String getName() {
        return name;
    }
}


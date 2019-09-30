/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ee.ria.dhx

import org.apache.http.Header
import org.apache.http.HttpEntity
import org.apache.http.NameValuePair
import org.apache.http.entity.ContentType
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ContentBody
import org.apache.http.message.BasicNameValuePair

import java.nio.charset.Charset

import static io.restassured.internal.common.assertion.AssertParameter.notNull

/**
 * This is essentially a copy of {@link io.restassured.internal.multipart.RestAssuredMultiPartEntity } that sets the Content-Type type parameter - https://tools.ietf.org/html/rfc2387#section-3.1.
 */
class DHXMultiPartEntity implements HttpEntity {

    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private final static char[] MULTIPART_CHARS =
            "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
                    .toCharArray();

    private final MultipartEntityBuilder builder;
    private volatile HttpEntity entity;

    public DHXMultiPartEntity(String subType, String charset, HttpMultipartMode mode, String boundary) {
        notNull(subType, "Multipart sub type");
        this.builder = MultipartEntityBuilder.create()
                .setMode(mode)
                .setCharset(charset == null ? null : Charset.forName(charset.trim()))
                .setContentType(ContentType.create("multipart/${subType.trim()}",
                        new BasicNameValuePair("type", "text/xml"),
                        new BasicNameValuePair("boundary", boundary)
                         ))
        new MultipartEntityBuilder().setContentType(ContentType.create('multipart/related', new BasicNameValuePair("type","text/xml"),new BasicNameValuePair("boundary","text/xml") )).build()

        this.entity = null;

    }

    private HttpEntity getEntity() {
        if (this.entity == null) {
            this.entity = this.builder.build();
        }
        return this.entity;
    }

    public void addPart(final FormBodyPart bodyPart) {
        this.builder.addPart(bodyPart);
        this.entity = null;
    }

    public void addPart(final String name, final ContentBody contentBody) {
        addPart(new FormBodyPart(name, contentBody));
    }

    public boolean isRepeatable() {
        return getEntity().isRepeatable();
    }

    public boolean isChunked() {
        return getEntity().isChunked();
    }

    public boolean isStreaming() {
        return getEntity().isStreaming();
    }

    public long getContentLength() {
        return getEntity().getContentLength();
    }

    public Header getContentType() {
        return getEntity().getContentType();
    }

    public Header getContentEncoding() {
        return getEntity().getContentEncoding();
    }

    public void consumeContent()
            throws IOException, UnsupportedOperationException {
        if (isStreaming()) {
            throw new UnsupportedOperationException(
                    "Streaming entity does not implement #consumeContent()");
        }
    }

    public InputStream getContent() throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "Multipart form entity does not implement #getContent()");
    }

    public void writeTo(final OutputStream outstream) throws IOException {
        getEntity().writeTo(outstream);
    }


}

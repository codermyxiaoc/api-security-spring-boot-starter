package cn.coderxiaoc.handlers;

import org.springframework.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DefaultHttpInputMessage implements org.springframework.http.HttpInputMessage {

    private final InputStream body;
    private final HttpHeaders headers;

    public DefaultHttpInputMessage(byte[] decryptedBody, HttpHeaders originalHeaders) {
        this.body = new ByteArrayInputStream(decryptedBody);
        this.headers = new HttpHeaders();
        if (originalHeaders != null) {
            this.headers.putAll(originalHeaders);
        }
        this.headers.setContentLength(decryptedBody.length);
    }

    public DefaultHttpInputMessage(byte[] decryptedBody) {
        this(decryptedBody, null);
    }

    @Override
    public InputStream getBody() throws IOException {
        return this.body;
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }
}

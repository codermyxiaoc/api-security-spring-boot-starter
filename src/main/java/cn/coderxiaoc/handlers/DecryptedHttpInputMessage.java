package cn.coderxiaoc.handlers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecryptedHttpInputMessage implements HttpInputMessage {

    private final InputStream body;
    private final HttpHeaders headers;

    public DecryptedHttpInputMessage(byte[] decryptedBody, HttpHeaders originalHeaders) {
        this.body = new ByteArrayInputStream(decryptedBody);
        this.headers = new HttpHeaders();
        if (originalHeaders != null) {
            this.headers.putAll(originalHeaders);
        }
        this.headers.setContentLength(decryptedBody.length);
    }

    public DecryptedHttpInputMessage(byte[] decryptedBody) {
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

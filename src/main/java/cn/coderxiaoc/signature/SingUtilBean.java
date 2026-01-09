package cn.coderxiaoc.signature;
import org.springframework.http.server.ServerHttpResponse;

import java.security.SecureRandom;

public class SingUtilBean {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private ServerHttpResponse response;
    public SingUtilBean(ServerHttpResponse response) {
        this.response = response;
    }
    public String getNonce(String field, Integer len) {
        StringBuilder result = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            int index = RANDOM.nextInt(CHARS.length());
            result.append(CHARS.charAt(index));
        }
        String nonce = result.toString();
        response.getHeaders().add(field, nonce);
        return nonce;
    }
    public String getNonce(String field) {
        return getNonce(field, 6);
    }
    public String getTimestamp(String field) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        response.getHeaders().add(field, timestamp);
        return timestamp;
    }
}

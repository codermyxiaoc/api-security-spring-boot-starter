package cn.coderxiaoc.signature;
import org.springframework.http.server.ServerHttpResponse;

public class SingUtilBean {
    private ServerHttpResponse response;
    public SingUtilBean(ServerHttpResponse response) {
        this.response = response;
    }
    private static final String  str = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public String getNonce(String field, Integer len) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < len; i++) {
            int index = (int) (Math.random() * str.length());
            result.append(str.charAt(index));
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

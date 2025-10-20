package cn.coderxiaoc.signature;

import com.alibaba.fastjson2.JSONObject;
import java.util.Map;

public class SignatureParams {
    private final Map<String, String> header;
    private final Map<String, String> body;
    public SignatureParams(Map<String, String> header, Map<String, String> body) {
        this.header = header;
        this.body = body;
    }
    public String header(String key) {
        return header.get(key);
    }
    public String body(String key) {
        return body.get(key);
    }
    public String bodyJson() {
        return JSONObject.toJSONString(body);
    }
}

package cn.coderxiaoc.signature;

import com.alibaba.fastjson2.JSONObject;
import java.util.Map;

public class SignatureParams {
    private final Map<String, Object> header;
    private final Map<String, Object> body;
    public SignatureParams(Map<String, Object> header, Map<String, Object> body) {
        this.header = header;
        this.body = body;
    }
    public String header(String key) {
        return valueAsString(header.get(key));
    }
    public String body(String key) {
        return valueAsString(body.get(key));
    }
    public String bodyJson() {
        return JSONObject.toJSONString(body);
    }
    private String valueAsString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}

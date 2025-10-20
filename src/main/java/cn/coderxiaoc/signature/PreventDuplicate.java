package cn.coderxiaoc.signature;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;

public interface PreventDuplicate {
    boolean preventDuplicate(HttpInputMessage inputMessage, MethodParameter parameter);
}

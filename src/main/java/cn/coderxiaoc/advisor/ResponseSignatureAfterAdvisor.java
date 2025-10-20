package cn.coderxiaoc.advisor;

import cn.coderxiaoc.annotation.Signature;
import cn.coderxiaoc.enums.RuntimeType;
import cn.coderxiaoc.signature.ResponseSignatureAbstract;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
@Log4j2
public class ResponseSignatureAfterAdvisor extends ResponseSignatureAbstract<Object> implements Ordered {
    public ResponseSignatureAfterAdvisor(ApplicationContext applicationContext, cn.coderxiaoc.signature.Signature signature) {
        super(applicationContext, signature);
    }

    @Override
    public int getOrder() {
        return  Integer.MIN_VALUE + 2;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean isSupport = super.supports(returnType, converterType);
        Signature signatureAnnotation = getSignatureAnnotation(returnType);
        if (signatureAnnotation == null) {
            return false;
        }
        return isSupport &&  signatureAnnotation.runtimeType() == RuntimeType.AFTER;
    }
}

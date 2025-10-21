package cn.coderxiaoc.advisor;

import cn.coderxiaoc.annotation.Verification;
import cn.coderxiaoc.enums.RuntimeType;
import cn.coderxiaoc.property.SignatureProperty;
import cn.coderxiaoc.signature.RequestVerificationAbstract;
import cn.coderxiaoc.signature.Signature;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

@Log4j2
@ControllerAdvice
public class RequestVerificationBeforeAdvisor extends RequestVerificationAbstract implements Ordered {

    public RequestVerificationBeforeAdvisor(ApplicationContext applicationContext, Signature  signature, SignatureProperty signatureProperty) {
        super(applicationContext, signature, signatureProperty);

    }

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean isSupport = super.supports(methodParameter, targetType, converterType);
        Verification verificationAnnotation = getVerificationAnnotation(methodParameter);

        return isSupport && verificationAnnotation.runtimeType() == RuntimeType.BEFORE;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return super.beforeBodyRead(inputMessage, parameter, targetType, converterType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return inputMessage;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}

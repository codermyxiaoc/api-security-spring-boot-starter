package cn.coderxiaoc.signature;

import cn.coderxiaoc.annotation.Verification;
import cn.coderxiaoc.exception.RequestTimeoutException;
import cn.coderxiaoc.exception.verify.*;
import cn.coderxiaoc.handlers.DefaultHttpInputMessage;
import cn.coderxiaoc.property.SignatureProperty;
import cn.coderxiaoc.utils.TimeoutVerifyUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public abstract class RequestVerificationAbstract  implements RequestBodyAdvice {
    private final ApplicationContext applicationContext;
    private final Signature signature;
    private final SignatureProperty signatureProperty;
    private PreventDuplicate preventDuplicate;
    private final Class<Verification> verificationAnnotation = Verification.class;
    public RequestVerificationAbstract(ApplicationContext applicationContext, Signature  signature, SignatureProperty signatureProperty) {
        this.signature = signature;
        this.applicationContext = applicationContext;
        this.signatureProperty = signatureProperty;

        if (signatureProperty.getEnablePreventDuplicate()) {
            PreventDuplicate preventDuplicateBean = applicationContext.getBean(PreventDuplicate.class);
            if (preventDuplicateBean == null) {
                throw new IllegalArgumentException("PreventDuplicate is required when enablePreventDuplicate is true");
            }
            this.preventDuplicate = preventDuplicateBean;
        }

    }



    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasMethodAnnotation(verificationAnnotation) || methodParameter.getContainingClass().isAnnotationPresent(verificationAnnotation);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        Verification verification = getVerificationAnnotation(parameter);
        this.preventDuplicate(inputMessage, parameter);
        this.timeout(inputMessage, verification);
        byte[] body = StreamUtils.copyToByteArray(inputMessage.getBody());
        ParamsParse paramsParse = this.getParamsParse(inputMessage, body);
        String singString = paramsParse.parse(verification.value());
        log.info("RequestVerificationAdvisor - params parse text: {}", singString);
        this.checkSignature(singString, verification, inputMessage);
        return new DefaultHttpInputMessage(body, inputMessage.getHeaders());
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Verification verification = getVerificationAnnotation(parameter);
        this.preventDuplicate(inputMessage, parameter);
        this.timeout(inputMessage, verification);
        ParamsParse paramsParse = this.getParamsParse(inputMessage, body);
        String singString = paramsParse.parse(verification.value());
        log.info("RequestVerificationAdvisor - params parse text: {}", singString);
        this.checkSignature(singString, verification, inputMessage);
        return body;
    }

    protected Verification getVerificationAnnotation(MethodParameter parameter) {
        Verification verification = parameter.getMethodAnnotation(verificationAnnotation);
        if (verification == null) {
            verification = parameter.getContainingClass().getAnnotation(verificationAnnotation);
        }
        return verification;
    }

    protected void preventDuplicate(HttpInputMessage inputMessage, MethodParameter parameter) {
        if (!this.signatureProperty.getEnablePreventDuplicate()) {
            return;
        }
        boolean flag = this.preventDuplicate.preventDuplicate(inputMessage, parameter);
        if (!flag) {
            throw new PreventDuplicateException("duplicate request has occurred");
        }
    }
    protected void timeout(HttpInputMessage inputMessage, Verification verification) {
        if (!this.signatureProperty.getEnableTimeout()) {
            return;
        }
        boolean flag = TimeoutVerifyUtil.timeoutVerify(inputMessage,verification , this.signatureProperty.getPreventDuplicateTimeout(), this.signatureProperty.getTimeoutField());
        if (flag) {
            throw new RequestTimeoutException("timeoutValue is out of range");
        }
    }

    private void checkSignature(String singString, Verification verification, HttpInputMessage inputMessage) {
        if (StringUtils.hasText(singString)) {
            String signatureField = verification.signatureField();
            if (!StringUtils.hasText(signatureField)) {
                log.error("RequestVerificationAdvisor - {}", "signatureField in @Verification is empty");
                throw new InvalidSignatureFieldException("signatureField in @Verification is empty");
            }

            List<String> signatureValues = inputMessage.getHeaders().get(signatureField);
            if (CollectionUtils.isEmpty(signatureValues)) {
                log.error("RequestVerificationAdvisor - {}", String.format("Signature field [%s] not found in request headers", signatureField));
                throw new SignatureMissingException(String.format("Signature field [%s] not found in request headers", signatureField));
            }
            String signatureValue = signatureValues.get(0);
            if (!this.signature.verify(signatureValue, singString)) {
                log.error("RequestVerificationAdvisor - {}", "Signature verification failed");
                throw new SignatureMissingException("Signature verification failed");
            }
            log.info("RequestVerificationAdvisor - header sing test: {}", signatureValue);

            boolean verify = signature.verify(signatureValue, singString);

            if (!verify) {
                String errorMsg = String.format("Signature verification failed Received signature: [%s], Verified parameters: [%s]",
                        signatureValue, singString);
                log.error("RequestVerificationBeforeAdvisor - {}", errorMsg);
                throw new SignatureMismatchException(errorMsg);
            }
        }
    }

    protected ParamsParse getParamsParse(HttpInputMessage inputMessage, byte[] body) {
        try {
            ParamsParseAbstract paramsParse = new DefaultParamsParse(applicationContext);
            paramsParse.initEvaluationContext(context -> {
                Map<String, String> headerMap = new HashMap<>();
                inputMessage.getHeaders().entrySet().forEach(entry -> {
                    headerMap.put(entry.getKey(), entry.getValue().get(0));
                });

                Map<String, String> bodyMap = JSON.parseObject(new String(body), HashMap.class);

                SignatureParams params = new SignatureParams(headerMap, bodyMap);
                context.setVariable("params", params);
            });
            return paramsParse;
        } catch (Exception e) {
           throw new CreateParamsParseException(e.getMessage());
        }
    }
    protected ParamsParse getParamsParse(HttpInputMessage inputMessage, Object body) {
        try {
            ParamsParseAbstract paramsParse = new DefaultParamsParse(applicationContext);
            paramsParse.initEvaluationContext(context -> {
                Map<String, String> headerMap = new HashMap<>();
                inputMessage.getHeaders().entrySet().forEach(entry -> {
                    headerMap.put(entry.getKey(), entry.getValue().get(0));
                });

                Map<String, String> bodyMap = JSON.parseObject(JSONObject.toJSONString(body), HashMap.class);

                SignatureParams params = new SignatureParams(headerMap, bodyMap);
                context.setVariable("params", params);
            });
            return paramsParse;
        } catch (Exception e) {
            throw new CreateParamsParseException(e.getMessage());
        }
    }
}

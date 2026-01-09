package cn.coderxiaoc.signature;

import cn.coderxiaoc.annotation.Signature;
import cn.coderxiaoc.exception.signature.SignatureParamsEmptyException;
import cn.coderxiaoc.exception.verify.CreateParamsParseException;
import cn.coderxiaoc.exception.verify.InvalidSignatureFieldException;
import cn.coderxiaoc.exception.verify.SignatureVerificationUnknownException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public abstract class ResponseSignatureAbstract<T> implements ResponseBodyAdvice<T> {
    private final SignatureExecutor signatureExecutor;
    private final Class<Signature> signatureAnnotation = Signature.class;
    private final ApplicationContext applicationContext;
    public ResponseSignatureAbstract(ApplicationContext applicationContext, SignatureExecutor signatureExecutor) {
        this.applicationContext = applicationContext;
        this.signatureExecutor = signatureExecutor;
    }
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(Signature.class) || returnType.getContainingClass().isAnnotationPresent(Signature.class);
    }

    @Override
    public T beforeBodyWrite(T body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
       try {
           Signature signature = getSignatureAnnotation(returnType);

           String signatureField = signature.signatureField();
           if (!StringUtils.hasText(signatureField)) {
               log.error("ResponseSignatureAdvisor - {}", "signatureField in @Signature is empty");
               throw new InvalidSignatureFieldException("signatureField in @Signature is empty");
           }
           ParamsParse paramsParse = getParamsParse(body, request, response, signature);
           String params = paramsParse.parse(signature.value());
           log.info("ResponseSignatureAdvisor - params parse text: {}", params);

           if (!StringUtils.hasText(params)) {
               log.error("ResponseSignatureAdvisor - {}", "Parsed signature parameters are empty");
               throw new SignatureParamsEmptyException("Parsed signature parameters are empty");
           }
           String algorithm = resolvePlaceholder(signature.algorithm());
           String sign = this.signatureExecutor.sign(params, algorithm);
           log.info("ResponseSignatureAdvisor - params sign text: {}", sign);
           response.getHeaders().add(signatureField, sign);
           return body;
       } catch (Exception e) {
           log.error("ResponseSignatureAdvisor - {}", "Unknown error during signature generation", e);
           throw new SignatureVerificationUnknownException("Unknown error during signature generation", e);
       }
    }

    protected Signature getSignatureAnnotation(MethodParameter parameter) {
        Signature signature = parameter.getMethodAnnotation(signatureAnnotation);
        if (signature == null) {
            signature = parameter.getContainingClass().getAnnotation(signatureAnnotation);
        }
        return signature;
    }
    protected ParamsParse getParamsParse(T body, ServerHttpRequest request, ServerHttpResponse response, Signature signature) {
        try {
            Map<String, Object> headerMap = new HashMap<>();
            request.getHeaders().forEach((key, value) -> headerMap.put(key, value.get(0)));

            Map<String, Object> bodyMap;
            try {
                bodyMap = JSON.parseObject(JSONObject.toJSONString(body), HashMap.class);
            } catch (JSONException e) {
                bodyMap = new HashMap<>();
                if (body instanceof String) {
                    bodyMap.put("data", body.toString());
                } else {
                    bodyMap.put("data", JSONObject.toJSONString(body));
                }
            }
            if (bodyMap == null) {
                bodyMap = new HashMap<>();
                bodyMap.put("data", body == null ? "" : body.toString());
            }
            SignatureParams params = new SignatureParams(headerMap, bodyMap);
            SingUtilBean singUtilBean = new SingUtilBean(response);

            ParamsParseAbstract paramsParse = new DefaultParamsParse(applicationContext, signature.delimiter(), signature.splitter());
            paramsParse.initEvaluationContext(context -> {
                context.setVariable("params", params);
                context.setVariable("request", request);
                context.setVariable("response", response);
                context.setVariable("sing", singUtilBean);
            });
            return paramsParse;
        } catch (Exception e) {
            throw new CreateParamsParseException(e.getMessage());
        }
    }

    private String resolvePlaceholder(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return applicationContext.getEnvironment().resolvePlaceholders(value);
    }
}

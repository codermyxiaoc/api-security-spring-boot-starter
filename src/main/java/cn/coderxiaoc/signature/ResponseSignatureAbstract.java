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
    private final cn.coderxiaoc.signature.Signature signature;
    private final Class<Signature> signatureAnnotation = Signature.class;
    private final ApplicationContext applicationContext;
    public ResponseSignatureAbstract(ApplicationContext applicationContext, cn.coderxiaoc.signature.Signature signature) {
        this.applicationContext = applicationContext;
        this.signature = signature;
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
           ParamsParse paramsParse = getParamsParse(body, request, response);
           String params = paramsParse.parse(signature.value());
           log.info("ResponseSignatureAdvisor - params parse text: {}", params);

           if (!StringUtils.hasText(params)) {
               log.error("ResponseSignatureAdvisor - {}", "Parsed signature parameters are empty");
               throw new SignatureParamsEmptyException("Parsed signature parameters are empty");
           }
           String sign = this.signature.sign(params);
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
    protected ParamsParse getParamsParse(T body, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            Map<String, String> headerMap = new HashMap<>();
            request.getHeaders().entrySet().forEach(entry -> {
                headerMap.put(entry.getKey(), entry.getValue().get(0));
            });

            Map<String, String> bodyMap;
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
            SignatureParams params = new SignatureParams(headerMap, bodyMap);
            SingUtilBean singUtilBean = new SingUtilBean(response);

            ParamsParseAbstract paramsParse = new DefaultParamsParse(applicationContext);
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
}

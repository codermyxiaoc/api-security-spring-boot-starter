package cn.coderxiaoc.advisor;

import cn.coderxiaoc.annotation.Decrypt;
import cn.coderxiaoc.encrypt.Cipher;
import cn.coderxiaoc.exception.decrypt.*;
import cn.coderxiaoc.handlers.DecryptedHttpInputMessage;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * 请求体解密增强器
 * 作用：对标注 @Decrypt 注解的方法/类，自动解密请求体数据后再进行参数绑定
 */
@ControllerAdvice
@Log4j2
public class RequestDecryptAdvisor implements RequestBodyAdvice {
    private static final String DECRYPT_RESULT_FIELD_ERROR_MSG = "Non-object type data cannot extract the specified field, nor can it perform object merging or encryption";
    private static final String DECRYPT_INVALID_JSON_MSG = "Original request data is not a valid JSON format, and decryptField cannot be parsed";
    private final Cipher cipher;

    public RequestDecryptAdvisor(Cipher cipher) {
        this.cipher = cipher;
    }


    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean isSupport = methodParameter.hasMethodAnnotation(Decrypt.class)
                || methodParameter.getContainingClass().isAnnotationPresent(Decrypt.class);
        // 补充日志：打印支持状态+目标方法，便于定位请求
        log.debug("RequestDecryptAdvisor support check - method: {}, isSupport: {}",
                methodParameter.getMethod().getName(), isSupport);
        return isSupport;
    }


    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        try {
            byte[] encryptedData = StreamUtils.copyToByteArray(inputMessage.getBody());
            if (ObjectUtils.isEmpty(encryptedData)) {
                log.debug("RequestDecryptAdvisor - empty encrypted data, skip decrypt");
                return inputMessage;
            }
            String encryptedDataStr = new String(encryptedData, StandardCharsets.UTF_8);
            log.info("RequestDecryptAdvisor - original encrypted data (method: {}): {}",
                    parameter.getMethod().getName(), encryptedDataStr);

            Decrypt decryptAnnotation = parameter.getMethodAnnotation(Decrypt.class);
            if (decryptAnnotation == null) {
                decryptAnnotation = parameter.getContainingClass().getAnnotation(Decrypt.class);
                log.debug("RequestDecryptAdvisor - use class level @Decrypt: {}", parameter.getContainingClass().getSimpleName());
            }

            String dataToDecrypt = encryptedDataStr;
            String decryptField = decryptAnnotation.decryptField();
            if (StringUtils.hasText(decryptField)) {
                try {
                    JSONObject encryptedReqJson = JSONObject.parseObject(encryptedDataStr);
                    dataToDecrypt = encryptedReqJson.getString(decryptField);
                    if (!StringUtils.hasText(dataToDecrypt)) {
                        throw new DecryptFieldEmptyException("Decrypt field [" + decryptField + "] is empty in request");
                    }
                    log.debug("RequestDecryptAdvisor - extract decrypt field [{}]: {}", decryptField, dataToDecrypt);
                } catch (JSONException e) {
                    throw new DecryptInvalidJsonException(DECRYPT_INVALID_JSON_MSG, e);
                }
            }

            String secretKey = StringUtils.hasText(decryptAnnotation.value())
                    ? decryptAnnotation.value()
                    : decryptAnnotation.secretKey();


            byte[] decryptedBytes;
            try {
                decryptedBytes = cipher.decrypt(dataToDecrypt, secretKey);
                if (ObjectUtils.isEmpty(decryptedBytes)) {
                    throw new DecryptFailedException("Decrypt result is empty, check cipher or secret key");
                }
            } catch (Exception e) {
                throw new DecryptFailedException("Decrypt failed (data: " + dataToDecrypt + ")", e);
            }
            String decryptedStr = new String(decryptedBytes, StandardCharsets.UTF_8);
            log.info("RequestDecryptAdvisor - decrypted success (method: {}): {}",
                    parameter.getMethod().getName(), decryptedStr);

            byte[] finalDecryptedData = decryptedBytes;
            if (decryptAnnotation.merge() && StringUtils.hasText(decryptField)) {
                try {
                    JSONObject mergedReqJson = JSONObject.parseObject(encryptedDataStr);
                    try {
                        mergedReqJson.put(decryptField, JSONObject.parseObject(decryptedStr));
                    } catch (JSONException e) {
                        mergedReqJson.put(decryptField, decryptedStr);
                    }
                    finalDecryptedData = mergedReqJson.toString().getBytes(StandardCharsets.UTF_8);
                    log.debug("RequestDecryptAdvisor - merge decrypt result to field [{}], merged data: {}",
                            decryptField, new String(finalDecryptedData, StandardCharsets.UTF_8));
                } catch (JSONException e) {
                    throw new DecryptInvalidJsonException(DECRYPT_INVALID_JSON_MSG, e);
                }
            }

            String decryptResultField = decryptAnnotation.decryptResultField();
            if (StringUtils.hasText(decryptResultField)) {
                try {
                    String mergedDataStr = new String(finalDecryptedData, StandardCharsets.UTF_8);

                    JSONObject mergedResultJson = JSONObject.parseObject(mergedDataStr);
                    String resultFieldValue = mergedResultJson.getString(decryptResultField);

                    if (!StringUtils.hasText(resultFieldValue)) {
                        throw new DecryptResultFieldEmptyException("Decrypt result field [" + decryptResultField + "] is empty (after merge)");
                    }
                    finalDecryptedData = resultFieldValue.getBytes(StandardCharsets.UTF_8);
                    log.debug("RequestDecryptAdvisor - extract result field [{}] from merged data: {}",
                            decryptResultField, resultFieldValue);
                } catch (JSONException e) {
                    throw new DecryptResultFieldException(DECRYPT_RESULT_FIELD_ERROR_MSG, e);
                }
            }
            log.info("RequestDecryptAdvisor - final decrypted data (method: {}): {}",
                    parameter.getMethod().getName(), new String(finalDecryptedData));
            return new DecryptedHttpInputMessage(finalDecryptedData, inputMessage.getHeaders());

        } catch (IOException e) {
            throw new DecryptIOException("Read encrypted data failed", e);
        } catch (DecryptBaseException e) {
            log.error("RequestDecryptAdvisor - decrypt exception (method: {}): {}",
                    parameter.getMethod().getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 捕获其他未知异常，封装为解密未知异常
            throw new DecryptUnknownException("Unknown decrypt exception (method: " + parameter.getMethod().getName() + ")", e);
        }
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        log.debug("RequestDecryptAdvisor - handle empty body (method: {})", parameter.getMethod().getName());
        return body;
    }
}

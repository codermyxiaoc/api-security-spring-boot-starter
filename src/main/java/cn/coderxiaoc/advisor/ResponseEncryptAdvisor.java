package cn.coderxiaoc.advisor;

import cn.coderxiaoc.annotation.Encrypt;
import cn.coderxiaoc.encrypt.Cipher;
import cn.coderxiaoc.exception.encrypt.*;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.nio.charset.StandardCharsets;

/**
 * 响应体加密增强器
 * 作用：对标注 @Encrypt 注解的方法/类，自动加密响应体数据后返回给客户端
 */
@ControllerAdvice
@Log4j2
// 明确泛型为 Object，避免 raw type 警告
public class ResponseEncryptAdvisor implements ResponseBodyAdvice<Object> {

    // 英文常量：错误信息（统一维护，符合技术场景表达）
    private static final String ENCRYPT_SECRET_KEY_EMPTY_MSG = "Encryption key is empty, cannot perform encryption";
    private static final String ENCRYPT_INVALID_JSON_MSG = "Response data is not valid JSON format, cannot parse encryptField";
    private static final String ENCRYPT_FIELD_EMPTY_MSG = "Encrypt field [%s] is empty in response data";
    private static final String ENCRYPT_RESULT_EMPTY_MSG = "Encryption result is empty, check cipher or secret key";
    private static final String UNSUPPORTED_MEDIA_TYPE_MSG = "Unsupported response media type [%s], only application/json is supported for encryption";

    // 加密器（构造注入，参数名与成员变量一致）
    private final Cipher cipher;

    /**
     * 构造注入加密器
     * @param cipher 加密器实例（由Spring容器注入，需确保Cipher已正确配置）
     */
    public ResponseEncryptAdvisor(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * 判断当前响应是否需要执行加密逻辑
     * 规则：1.方法上有 @Encrypt 注解 或 类上有 @Encrypt 注解；2.响应类型为 application/json
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        boolean isSupport = returnType.hasMethodAnnotation(Encrypt.class)
                || returnType.getContainingClass().isAnnotationPresent(Encrypt.class);
        log.debug("ResponseEncryptAdvisor support check - method: {}, isSupport: {}",
                returnType.getMethod().getName(), isSupport);
        return isSupport;
    }

    /**
     * 响应体写出前执行加密逻辑
     * 核心流程：1.校验响应类型 → 2.获取 @Encrypt 注解 → 3.解析响应数据 → 4.提取待加密字段 → 5.获取加密密钥 → 6.执行加密 → 7.合并加密结果 → 8.返回加密后数据
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            if (ObjectUtils.isEmpty(body)) {
                throw new EncryptBodyEmptyException("Response body is empty, cannot encrypt");
            }
            if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) {
                throw new EncryptUnsupportedMediaTypeException(String.format(UNSUPPORTED_MEDIA_TYPE_MSG, selectedContentType));
            }

            String originalBodyStr = JSONObject.toJSONString(body);
            log.info("ResponseEncryptAdvisor - original response data (method: {}): {}",
                    returnType.getMethod().getName(), originalBodyStr);

            // 3. 获取 @Encrypt 注解（方法注解优先级高于类注解）
            Encrypt encryptAnnotation = returnType.getMethodAnnotation(Encrypt.class);
            if (encryptAnnotation == null) {
                encryptAnnotation = returnType.getContainingClass().getAnnotation(Encrypt.class);
                log.debug("ResponseEncryptAdvisor - use class level @Encrypt: {}", returnType.getContainingClass().getSimpleName());
            }

            String dataToEncrypt = originalBodyStr;
            String encryptField = encryptAnnotation.encryptField();
            if (StringUtils.hasText(encryptField)) {
                try {
                    JSONObject originalBodyJson = JSONObject.parseObject(originalBodyStr);
                    dataToEncrypt = originalBodyJson.getString(encryptField);
                    if (!StringUtils.hasText(dataToEncrypt)) {
                        throw new EncryptFieldEmptyException(String.format(ENCRYPT_FIELD_EMPTY_MSG, encryptField));
                    }
                    log.debug("ResponseEncryptAdvisor - extract encrypt field [{}]: {}", encryptField, dataToEncrypt);
                } catch (JSONException e) {
                    throw new EncryptInvalidJsonException(ENCRYPT_INVALID_JSON_MSG, e);
                }
            }

            String secretKey = StringUtils.hasText(encryptAnnotation.value())
                    ? encryptAnnotation.value()
                    : encryptAnnotation.secretKey();
            byte[] encryptedBytes;
            try {
                encryptedBytes = cipher.encrypt(dataToEncrypt.getBytes(StandardCharsets.UTF_8), secretKey);
                if (ObjectUtils.isEmpty(encryptedBytes)) {
                    throw new EncryptFailedException(ENCRYPT_RESULT_EMPTY_MSG);
                }
            } catch (Exception e) {
                throw new EncryptFailedException("Encryption failed (data: " + dataToEncrypt + ")", e);
            }
            String encryptedStr = new String(encryptedBytes, StandardCharsets.UTF_8);
            log.info("ResponseEncryptAdvisor - encryption success (method: {}): {}",
                    returnType.getMethod().getName(), encryptedStr);

            if (encryptAnnotation.merge() && StringUtils.hasText(encryptField)) {
                try {
                    JSONObject mergedBodyJson = JSONObject.parseObject(originalBodyStr);
                    mergedBodyJson.put(encryptField, encryptedStr);
                    String mergedBodyStr = mergedBodyJson.toString();
                    log.debug("ResponseEncryptAdvisor - merge encrypted result to field [{}], merged data: {}",
                            encryptField, mergedBodyStr);
                    return JSONObject.parseObject(mergedBodyStr);
                } catch (JSONException e) {
                    throw new EncryptInvalidJsonException(ENCRYPT_INVALID_JSON_MSG, e);
                }
            }
            log.info("ResponseEncryptAdvisor - final encrypted data (method: {}): {}",
                    returnType.getMethod().getName(), encryptedStr);
            return encryptedStr;

        } catch (EncryptBaseException e) {

            log.error("ResponseEncryptAdvisor - encryption exception (method: {}): {}",
                    returnType.getMethod().getName(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 捕获未知异常，封装为加密未知异常
            throw new EncryptUnknownException("Unknown encryption exception (method: " + returnType.getMethod().getName() + ")", e);
        }
    }
}

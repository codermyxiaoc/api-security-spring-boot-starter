package cn.coderxiaoc.signature;

import cn.coderxiaoc.property.SignatureProperty;
import org.springframework.core.MethodParameter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public class RedisPreventDuplicate extends PreventDuplicateAbstract {
    private SignatureProperty signatureProperty;
    private StringRedisTemplate redisTemplate;
    public RedisPreventDuplicate(SignatureProperty signatureProperty, StringRedisTemplate redisTemplate) {
        if (redisTemplate == null) {
            throw new IllegalStateException("StringRedisTemplate is not available");
        }
        this.signatureProperty = signatureProperty;
        this.redisTemplate = redisTemplate;
    }
    @Override
    public boolean preventDuplicate(HttpInputMessage inputMessage, MethodParameter parameter) {
        String preventDuplicateField = getPreventDuplicateField(parameter);
        String preventDuplicateValue = getPreventDuplicateValue(preventDuplicateField, inputMessage);
        return redisTemplate.opsForValue().setIfAbsent(getPrefix() + ":" + preventDuplicateValue, preventDuplicateValue, getPreventDuplicateTimeout(parameter), getPreventDuplicateTimeUnit( parameter));
    }
    private String getPrefix() {
        return signatureProperty.getPreventDuplicatePrefix();
    }

    @Override
    SignatureProperty getSignatureProperty() {
        return signatureProperty;
    }
}

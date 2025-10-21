package cn.coderxiaoc.signature;


import cn.coderxiaoc.annotation.Verification;
import cn.coderxiaoc.property.SignatureProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

public abstract class PreventDuplicateAbstract implements PreventDuplicate {
    private Verification getVerification(MethodParameter methodParameter) {
        Verification annotationVerification = methodParameter.getMethodAnnotation(Verification.class);
        if (annotationVerification == null) {
            annotationVerification = methodParameter.getContainingClass().getAnnotation(Verification.class);
        }
        return annotationVerification;
    }
    protected String getPreventDuplicateField(MethodParameter methodParameter) {
        Verification annotationVerification = getVerification(methodParameter);
        if (annotationVerification != null) {
            String preventDuplicateField = annotationVerification.preventDuplicateField();
            if (!StringUtils.hasText(preventDuplicateField)) {
                preventDuplicateField = getSignatureProperty().getPreventDuplicateField();
            }
            Assert.isTrue(StringUtils.hasText(preventDuplicateField), "preventDuplicateField is not available");
            return preventDuplicateField;
        }
        return "";
    }
    protected String getPreventDuplicateValue(String preventDuplicateField, HttpInputMessage inputMessage) {
        Assert.isTrue(StringUtils.hasText(preventDuplicateField), "preventDuplicateField is not available");
        String preventDuplicateValue = inputMessage.getHeaders().getFirst(preventDuplicateField);
        if (!StringUtils.hasText(preventDuplicateValue)) {
            throw new IllegalStateException(String.format("PreventDuplicate field [%s] not found in request headers", preventDuplicateField));
        }
        return preventDuplicateValue;
    }
    protected TimeUnit getPreventDuplicateTimeUnit(MethodParameter methodParameter) {
        Verification verification = getVerification(methodParameter);
        TimeUnit timeUnit = verification.timeUnit();
        if (timeUnit == TimeUnit.SECONDS) {
            if (timeUnit == getSignatureProperty().getPreventDuplicateTimeUnit()) {
                return timeUnit;
            } else  {
                timeUnit = getSignatureProperty().getPreventDuplicateTimeUnit();
            }
        }
        return timeUnit;
    }
    protected long getPreventDuplicateTimeout(MethodParameter methodParameter) {
        Verification verification = getVerification(methodParameter);
        long timeout = verification.timeout();
        if (timeout == 0L) {
            timeout = getSignatureProperty().getPreventDuplicateTimeout();
        }
        return timeout;
    }
    abstract SignatureProperty getSignatureProperty();

}

package cn.coderxiaoc.utils;

import cn.coderxiaoc.annotation.Verification;
import cn.coderxiaoc.exception.verify.SignatureMissingException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TimeoutVerifyUtil {
    public static boolean timeoutVerify(HttpInputMessage inputMessage,Verification  verificationAnnotation, long timeout, String timeoutField) {
        Assert.notNull(verificationAnnotation, "Verification annotation is required");
        List<String> timeoutFieldHeaders = inputMessage.getHeaders().get(verificationAnnotation.timeoutField());
        if (CollectionUtils.isEmpty(timeoutFieldHeaders)) {
           timeoutFieldHeaders = inputMessage.getHeaders().get(timeoutField);
        }
        if (CollectionUtils.isEmpty(timeoutFieldHeaders)) {
            throw new SignatureMissingException("field timeoutField not found in request headers ");
        }
        String timeoutValue = timeoutFieldHeaders.get(0);
        try {
            long timeoutDistance = Long.parseLong(timeoutValue);
            long timeoutTarget = System.currentTimeMillis();
            long difftime = timeoutTarget - timeoutDistance;
            long convert = verificationAnnotation.timeUnit().convert(difftime, TimeUnit.MILLISECONDS);
            if (verificationAnnotation.timeout() == 0l) {
                if (convert > timeout) {
                   return true;
                }
            } else {
                if (convert > verificationAnnotation.timeout()) {
                    return true;
                }
            }
        }catch ( NumberFormatException e) {
            throw new IllegalArgumentException("timeoutValue is not a number");
        }
        return false;
    }
    private static Verification getVerificationAnnotation(MethodParameter parameter) {
        Verification verificationAnnotation = parameter.getMethodAnnotation(Verification.class);
        if (verificationAnnotation != null) {
            verificationAnnotation = parameter.getContainingClass().getAnnotation(Verification.class);
        }
        return verificationAnnotation;
    }
}

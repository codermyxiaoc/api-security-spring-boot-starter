package cn.coderxiaoc.signature;


import cn.coderxiaoc.annotation.Verification;
import org.springframework.core.MethodParameter;

public abstract class PreventDuplicateAbstract implements PreventDuplicate {
    protected String getPreventDuplicateField(MethodParameter methodParameter) {
        Verification annotationVerification = methodParameter.getMethodAnnotation(Verification.class);
        if (annotationVerification == null) {
            annotationVerification = methodParameter.getContainingClass().getAnnotation(Verification.class);
        }
        if (annotationVerification != null) {
            return annotationVerification.preventDuplicateField();
        }
        return "";
    }

}

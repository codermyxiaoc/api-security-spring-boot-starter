package cn.coderxiaoc.annotation;

import cn.coderxiaoc.enums.RuntimeType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Verification {
    String value();
    RuntimeType runtimeType() default RuntimeType.BEFORE;
    String signatureField();
    String preventDuplicateField() default "";
    String timeoutField() default "";
    long timeout() default 0L;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    String delimiter() default "|";
    String splitter() default "&";
}

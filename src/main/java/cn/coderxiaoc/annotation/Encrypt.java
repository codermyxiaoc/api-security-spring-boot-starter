package cn.coderxiaoc.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Encrypt {
    @AliasFor("secretKey")
    String value() default "";
    String encryptField() default "";
    @AliasFor("value")
    String secretKey() default "";
    boolean merge() default true;
}

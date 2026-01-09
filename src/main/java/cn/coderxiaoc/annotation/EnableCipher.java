package cn.coderxiaoc.annotation;

import cn.coderxiaoc.enums.DefaultCipherMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableCipher {
    DefaultCipherMode mode() default DefaultCipherMode.AES;
}

package cn.coderxiaoc.annotation;

import cn.coderxiaoc.config.SignatureRegisterImportSelector;
import cn.coderxiaoc.enums.DefaultSignatureMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(SignatureRegisterImportSelector.class)
public @interface EnableSignature {
    DefaultSignatureMode mode() default DefaultSignatureMode.RSA;
}

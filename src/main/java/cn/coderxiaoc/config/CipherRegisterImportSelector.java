package cn.coderxiaoc.config;

import cn.coderxiaoc.annotation.EnableCipher;
import cn.coderxiaoc.enums.DefaultCipherMode;
import cn.coderxiaoc.property.AESCipherProperty;
import cn.coderxiaoc.property.RSACipherProperty;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class CipherRegisterImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> enableCipherAttributes = importingClassMetadata.getAnnotationAttributes(EnableCipher.class.getName());
        if (enableCipherAttributes == null) {
            return new String[0];
        }
        DefaultCipherMode mode = (DefaultCipherMode)enableCipherAttributes.get("mode");
        if (mode == DefaultCipherMode.NONE) {
            return new String[0];
        }
        if (mode == DefaultCipherMode.AES) {
            return new String[]{ AESCipherProperty.class.getName()};
        }
        return new String[]{ RSACipherProperty.class.getName()};
    }
}

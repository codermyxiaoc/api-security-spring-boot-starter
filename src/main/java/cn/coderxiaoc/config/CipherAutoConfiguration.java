package cn.coderxiaoc.config;

import cn.coderxiaoc.annotation.EnableCipher;
import cn.coderxiaoc.enums.DefaultCipherMode;
import cn.coderxiaoc.property.AESProperty;
import cn.coderxiaoc.property.RSAProperty;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class CipherAutoConfiguration implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> enableCipherAttributes = importingClassMetadata.getAnnotationAttributes(EnableCipher.class.getName());
        DefaultCipherMode mode = (DefaultCipherMode)enableCipherAttributes.get("mode");
        if (mode == DefaultCipherMode.AES) {
            return new String[]{ AESProperty.class.getName(), CipherConfiguration.class.getName()};
        }
        return new String[]{ RSAProperty.class.getName(), CipherConfiguration.class.getName()};
    }
}

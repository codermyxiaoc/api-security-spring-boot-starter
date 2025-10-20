package cn.coderxiaoc.config;

import cn.coderxiaoc.annotation.EnableSignature;
import cn.coderxiaoc.enums.DefaultSignatureMode;
import cn.coderxiaoc.property.MD5SignatureProperty;
import cn.coderxiaoc.property.RSACipherProperty;
import cn.coderxiaoc.property.RSASignatureProperty;
import cn.coderxiaoc.property.SignatureProperty;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

public class SignatureRegisterImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> enableSignatureAttributes = importingClassMetadata.getAnnotationAttributes(EnableSignature.class.getName());
        if (enableSignatureAttributes == null) {
            return new String[0];
        }
        DefaultSignatureMode mode = (DefaultSignatureMode)enableSignatureAttributes.get("mode");
        if (mode == DefaultSignatureMode.NONE) {
            return new String[]{ SignatureProperty.class.getName()};
        }
        if (mode == DefaultSignatureMode.RSA) {
            return new String[]{ SignatureProperty.class.getName(), RSASignatureProperty.class.getName()};
        }
        return new String[]{SignatureProperty.class.getName(), MD5SignatureProperty.class.getName()};
    }
}

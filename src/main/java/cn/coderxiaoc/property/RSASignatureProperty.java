package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "web-security.signature.rsa")
public class RSASignatureProperty {
    private String privateKey;
    private String publicKey;
    private String privatePath;
    private String publicPath;
}

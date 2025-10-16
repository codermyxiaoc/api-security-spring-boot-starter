package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "cipher.rsa")
@Data
public class RSAProperty {
    private String privateKey;
    private String publicKey;
    private String privatePath;
    private String publicPath;
}

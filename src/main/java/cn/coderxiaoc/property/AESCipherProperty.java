package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "web-security.cipher.aes")
public class AESCipherProperty {
    private String secretKey;
}

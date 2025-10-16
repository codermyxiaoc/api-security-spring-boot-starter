package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;



@ConfigurationProperties(prefix = "cipher.aes")
@Data
public class AESProperty {
    private String secretKey;
}

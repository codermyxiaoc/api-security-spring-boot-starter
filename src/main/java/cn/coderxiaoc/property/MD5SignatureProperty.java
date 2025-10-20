package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Data
@ConfigurationProperties(prefix = "web-security.signature.md5")
public class MD5SignatureProperty {
    private String secretKey;
    private String delimiter = "|";
}

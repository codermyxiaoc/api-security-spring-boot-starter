package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Data
@ConfigurationProperties(prefix = "web-security.signature")
public class SignatureProperty {
   private Boolean enablePreventDuplicate = false;
   private String preventDuplicateField = "nonce";
   private Long preventDuplicateTimeout = 5L;
   private Boolean enableTimeout = false;
   private String timeoutField = "timestamp";
   private Long timeout = 5L;
}

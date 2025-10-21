package cn.coderxiaoc.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;


@Data
@ConfigurationProperties(prefix = "web-security.signature")
public class SignatureProperty {
   private Boolean enablePreventDuplicate = false;
   private String preventDuplicateField = "x-s-nonce";
   private Long preventDuplicateTimeout = 5L;
   private TimeUnit preventDuplicateTimeUnit = TimeUnit.SECONDS;
   private String preventDuplicatePrefix = "preventDuplicate";
   private Boolean enableTimeout = false;
   private String timeoutField = "x-s-timestamp";
   private Long timeout = 5L;
   private Long inMemoryCleanInterval = 30L;
   private TimeUnit inMemoryCleanIntervalTimeUnit = TimeUnit.SECONDS;
}

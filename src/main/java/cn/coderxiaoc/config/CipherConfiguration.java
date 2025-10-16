package cn.coderxiaoc.config;


import cn.coderxiaoc.advisor.RequestDecryptAdvisor;
import cn.coderxiaoc.advisor.ResponseEncryptAdvisor;
import cn.coderxiaoc.encrypt.AESCipherDefault;
import cn.coderxiaoc.encrypt.Cipher;
import cn.coderxiaoc.encrypt.RSACipherDefault;
import cn.coderxiaoc.property.AESProperty;
import cn.coderxiaoc.property.RSAProperty;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties()

public class CipherConfiguration implements BeanClassLoaderAware {
    private ClassLoader classLoader;
    @Bean
    @ConditionalOnBean(AESProperty.class)
    @ConditionalOnMissingBean(Cipher.class)
    public Cipher aseEncrypt(AESProperty aesProperty) {
        return new AESCipherDefault(aesProperty);
    }
    @Bean
    @ConditionalOnBean(RSAProperty.class)
    @ConditionalOnMissingBean(Cipher.class)
    public Cipher rsaEncrypt(RSAProperty rsaProperty) {
        return new RSACipherDefault(rsaProperty, classLoader);
    }
    @Bean
    @ConditionalOnBean(Cipher.class)
    public RequestDecryptAdvisor requestDecryptAdvisor(Cipher cipher) {
        return new RequestDecryptAdvisor(cipher);
    }

    @Bean
    @ConditionalOnBean(Cipher.class)
    public ResponseEncryptAdvisor responseEncryptAdvisor(Cipher cipher) {
        return new ResponseEncryptAdvisor(cipher);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}

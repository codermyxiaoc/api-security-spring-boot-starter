package cn.coderxiaoc.config;


import cn.coderxiaoc.advisor.*;
import cn.coderxiaoc.cipher.AESCipherDefault;
import cn.coderxiaoc.cipher.Cipher;
import cn.coderxiaoc.cipher.RSACipherDefault;
import cn.coderxiaoc.property.*;
import cn.coderxiaoc.signature.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties()
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebSecurityAutoConfiguration {

    @Configuration
    static class CipherAutoConfiguration implements BeanClassLoaderAware  {
        private ClassLoader classLoader;
        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
        @Bean
        @ConditionalOnBean(AESCipherProperty.class)
        @ConditionalOnMissingBean(Cipher.class)
        public Cipher aseEncrypt(AESCipherProperty aesCipherProperty) {
            return new AESCipherDefault(aesCipherProperty);
        }
        @Bean
        @ConditionalOnBean(RSACipherProperty.class)
        @ConditionalOnMissingBean(Cipher.class)
        public Cipher rsaEncrypt(RSACipherProperty rsaCipherProperty) {
            return new RSACipherDefault(rsaCipherProperty, classLoader);
        }
        @Bean
        public RequestDecryptAdvisor requestDecryptAdvisor(Cipher cipher) {
            return new RequestDecryptAdvisor(cipher);
        }
        @Bean
        public ResponseEncryptAdvisor responseEncryptAdvisor(Cipher cipher) {
            return new ResponseEncryptAdvisor(cipher);
        }
    }
    @Configuration
    @ConditionalOnBean(SignatureProperty.class)
    static class SignatureAutoConfiguration implements ApplicationContextAware,BeanClassLoaderAware {
        private ApplicationContext applicationContext;
        private ClassLoader classLoader;
        @Bean
        @ConditionalOnBean(RSASignatureProperty.class)
        @ConditionalOnMissingBean(Signature.class)
        public Signature rsaSignature(RSASignatureProperty rsaSignatureProperty) {
            return new RSASignatureAbstractDefault(rsaSignatureProperty, classLoader);
        }
        @Bean
        @ConditionalOnBean(MD5SignatureProperty.class)
        @ConditionalOnMissingBean(Signature.class)
        public Signature md5Signature(MD5SignatureProperty md5SignatureProperty) {
            return new MD5SignatureAbstractDefault(md5SignatureProperty);
        }
        @Bean
        @ConditionalOnBean({Signature.class, SignatureProperty.class})
        public RequestVerificationBeforeAdvisor requestVerificationBeforeAdvisor(Signature signature, SignatureProperty signatureProperty) {
            return new RequestVerificationBeforeAdvisor(applicationContext, signature, signatureProperty);
        }
        @Bean
        @ConditionalOnBean({Signature.class, SignatureProperty.class})
        public RequestVerificationAfterAdvisor requestVerificationAfterAdvisor( Signature signature, SignatureProperty signatureProperty) {
            return new RequestVerificationAfterAdvisor( applicationContext, signature, signatureProperty);
        }
        @Bean
        @ConditionalOnBean({Signature.class, SignatureProperty.class})
        public ResponseSignatureBeforeAdvisor responseSignatureBeforeAdvisor(Signature signature) {
            return new ResponseSignatureBeforeAdvisor(applicationContext, signature);
        }
        @Bean
        @ConditionalOnBean({Signature.class, SignatureProperty.class})
        public ResponseSignatureAfterAdvisor responseSignatureAfterAdvisor(Signature signature) {
            return new ResponseSignatureAfterAdvisor(applicationContext,  signature);
        }
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }
    }
    @Configuration
    @ConditionalOnClass(name = "org.springframework.data.redis.core.StringRedisTemplate")
    static class RedisPreventDuplicateConfiguration {
        @Bean
        @ConditionalOnProperty(prefix = "web-security.signature", name = "enable-prevent-duplicate", havingValue = "true")
        @ConditionalOnMissingBean(PreventDuplicate.class)
        public RedisPreventDuplicate redisPreventDuplicate(
                SignatureProperty signature,
                StringRedisTemplate redisTemplate) {
            return new RedisPreventDuplicate(signature, redisTemplate);
        }
    }
    @Configuration
    @ConditionalOnMissingClass("org.springframework.data.redis.core.StringRedisTemplate")
    static class InMemoryPreventDuplicateConfiguration {
        @Bean
        @ConditionalOnProperty(prefix = "web-security.signature", name = "enable-prevent-duplicate", havingValue = "true")
        @ConditionalOnMissingBean(PreventDuplicate.class)
        public InMemoryPreventDuplicate inMemoryPreventDuplicate(
                SignatureProperty signature) {
            return new InMemoryPreventDuplicate(signature);
        }
    }


}

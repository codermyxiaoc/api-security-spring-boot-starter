package cn.coderxiaoc.config;


import cn.coderxiaoc.advisor.*;
import cn.coderxiaoc.cipher.*;
import cn.coderxiaoc.property.*;
import cn.coderxiaoc.signature.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConfigurationPropertiesScan("cn.coderxiaoc")
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
        public CipherRegistry cipherRegistry(ObjectProvider<Cipher> cipherProvider) {
            List<Cipher> ciphers = cipherProvider.orderedStream().collect(Collectors.toList());
            return new CipherRegistry(ciphers);
        }
        @Bean
        public CipherExecutor cipherExecutor(CipherRegistry cipherRegistry, CipherProperty cipherProperty) {
            return new CipherExecutor(cipherRegistry, cipherProperty);
        }
        @Bean(name = "aesCipher")
        @ConditionalOnProperty(prefix = "web-security.cipher.aes", name = "secret-key")
        @ConditionalOnMissingBean(name = "aesCipher")
        public Cipher aseEncrypt(AESCipherProperty aesCipherProperty) {
            return new AESCipherDefault(aesCipherProperty);
        }
        @Bean(name = "rsaCipher")
        @ConditionalOnExpression("('${web-security.cipher.rsa.private-key:}' != '' or '${web-security.cipher.rsa.private-path:}' != '') and ('${web-security.cipher.rsa.public-key:}' != '' or '${web-security.cipher.rsa.public-path:}' != '')")
        @ConditionalOnMissingBean(name = "rsaCipher")
        public Cipher rsaEncrypt(RSACipherProperty rsaCipherProperty) {
            return new RSACipherDefault(rsaCipherProperty, classLoader);
        }
        @Bean
        public RequestDecryptAdvisor requestDecryptAdvisor(CipherExecutor cipherExecutor, Environment environment) {
            return new RequestDecryptAdvisor(cipherExecutor, environment);
        }
        @Bean
        public ResponseEncryptAdvisor responseEncryptAdvisor(CipherExecutor cipherExecutor, Environment environment) {
            return new ResponseEncryptAdvisor(cipherExecutor, environment);
        }
    }
    @Configuration
    @ConditionalOnBean(SignatureProperty.class)
    static class SignatureAutoConfiguration implements ApplicationContextAware,BeanClassLoaderAware {
        private ApplicationContext applicationContext;
        private ClassLoader classLoader;
        @Bean
        public SignatureRegistry signatureRegistry(ObjectProvider<Signature> signatureProvider) {
            List<Signature> signatures = signatureProvider.orderedStream().collect(Collectors.toList());
            return new SignatureRegistry(signatures);
        }
        @Bean
        public SignatureExecutor signatureExecutor(SignatureRegistry signatureRegistry, SignatureProperty signatureProperty) {
            return new SignatureExecutor(signatureRegistry, signatureProperty);
        }
        @Bean(name = "rsaSignature")
        @ConditionalOnExpression("('${web-security.signature.rsa.private-key:}' != '' or '${web-security.signature.rsa.private-path:}' != '') and ('${web-security.signature.rsa.public-key:}' != '' or '${web-security.signature.rsa.public-path:}' != '')")
        @ConditionalOnMissingBean(name = "rsaSignature")
        public Signature rsaSignature(RSASignatureProperty rsaSignatureProperty) {
            return new RSASignatureAbstractDefault(rsaSignatureProperty, classLoader);
        }
        @Bean(name = "md5Signature")
        @ConditionalOnProperty(prefix = "web-security.signature.md5", name = "secret-key")
        @ConditionalOnMissingBean(name = "md5Signature")
        public Signature md5Signature(MD5SignatureProperty md5SignatureProperty) {
            return new MD5SignatureAbstractDefault(md5SignatureProperty);
        }
        @Bean
        public RequestVerificationBeforeAdvisor requestVerificationBeforeAdvisor(SignatureExecutor signatureExecutor, SignatureProperty signatureProperty) {
            return new RequestVerificationBeforeAdvisor(applicationContext, signatureExecutor, signatureProperty);
        }
        @Bean
        public RequestVerificationAfterAdvisor requestVerificationAfterAdvisor( SignatureExecutor signatureExecutor, SignatureProperty signatureProperty) {
            return new RequestVerificationAfterAdvisor( applicationContext, signatureExecutor, signatureProperty);
        }
        @Bean
        public ResponseSignatureBeforeAdvisor responseSignatureBeforeAdvisor(SignatureExecutor signatureExecutor) {
            return new ResponseSignatureBeforeAdvisor(applicationContext, signatureExecutor);
        }
        @Bean
        public ResponseSignatureAfterAdvisor responseSignatureAfterAdvisor(SignatureExecutor signatureExecutor) {
            return new ResponseSignatureAfterAdvisor(applicationContext,  signatureExecutor);
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

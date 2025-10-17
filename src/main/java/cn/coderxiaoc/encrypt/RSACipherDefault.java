package cn.coderxiaoc.encrypt;

import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.exception.ReadSecretKeyException;
import cn.coderxiaoc.property.RSAProperty;
import cn.coderxiaoc.utils.RSAUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSACipherDefault extends RSACipherAbstract {
    private final RSAProperty rsaProperty;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private ClassLoader classLoader;
    public RSACipherDefault(RSAProperty rsaProperty, ClassLoader classLoader) {
        super();
        Assert.notNull(rsaProperty, "rsa property is null");
        Assert.isTrue(StringUtils.hasText(rsaProperty.getPrivateKey()) || StringUtils.hasText(rsaProperty.getPrivatePath()), "private key is null");
        Assert.isTrue(StringUtils.hasText(rsaProperty.getPublicKey()) || StringUtils.hasText(rsaProperty.getPublicPath()), "public key is null");
        this.rsaProperty = rsaProperty;
        this.classLoader = classLoader;
        loadSecretKey();
    }
    @Override
    public byte[] encrypt(byte[] data, String publicKey) {
        if (StringUtils.hasText(publicKey)) {
            try {
                return encryptObjectSection(data, restorePublicKey(publicKey));
            } catch (Exception e) {
                throw new CipherException(e);
            }
        } else {
            try {
                return encryptObjectSection(data, this.publicKey);
            } catch (Exception e) {
                throw new CipherException(e);
            }
        }
    }

    @Override
    public byte[] decrypt(String data, String privateKey) {
        if (StringUtils.hasText(privateKey)) {
            try {
                return decryptObjectSection(data, restorePrivateKey(privateKey));
            } catch (Exception e) {
                throw new CipherException(e);
            }
        } else {
            try {
                return decryptObjectSection(data, this.privateKey);
            } catch (Exception e) {
                throw new CipherException(e);
            }
        }
    }
    private void loadSecretKey() {
        if (StringUtils.hasText(rsaProperty.getPrivateKey())){
            try {
                this.privateKey = restorePrivateKey(rsaProperty.getPrivateKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            InputStream resourceAsStream = null;
            try {

                resourceAsStream =  this.classLoader.getResourceAsStream(rsaProperty.getPrivatePath());
                byte[] bytes = IOUtils.toByteArray(resourceAsStream);
                this.privateKey = restorePrivateKey(RSAUtil.readPrivateKeyFromByte(bytes));

            } catch (Exception e) {
                throw new ReadSecretKeyException(e);
            } finally {
                if (resourceAsStream != null) {
                    IOUtils.closeQuietly(resourceAsStream);
                }
            }
        }
        if (StringUtils.hasText(rsaProperty.getPublicKey())){
            try {
                this.publicKey = restorePublicKey(rsaProperty.getPublicKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = this.classLoader.getResourceAsStream(rsaProperty.getPublicPath());
                byte[] bytes = IOUtils.toByteArray(resourceAsStream);
                this.publicKey = restorePublicKey(RSAUtil.readPublicKeyFromByte(bytes));
            } catch (Exception e) {
                throw new ReadSecretKeyException( e);
            } finally {
                if (resourceAsStream != null) {
                    IOUtils.closeQuietly(resourceAsStream);
                }
            }
        }

    }

}

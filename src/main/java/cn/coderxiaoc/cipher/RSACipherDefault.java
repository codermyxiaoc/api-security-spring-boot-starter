package cn.coderxiaoc.cipher;

import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.property.RSACipherProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


public class RSACipherDefault extends RSACipherAbstract {
    private final RSACipherProperty rsaCipherProperty;
    public RSACipherDefault(RSACipherProperty rsaCipherProperty, ClassLoader classLoader) {
        super();
        Assert.notNull(rsaCipherProperty, "rsa property is null");
        Assert.isTrue(StringUtils.hasText(rsaCipherProperty.getPrivateKey()) || StringUtils.hasText(rsaCipherProperty.getPrivatePath()), "private key is null");
        Assert.isTrue(StringUtils.hasText(rsaCipherProperty.getPublicKey()) || StringUtils.hasText(rsaCipherProperty.getPublicPath()), "public key is null");
        this.rsaCipherProperty = rsaCipherProperty;
        this.loadSecretKey(rsaCipherProperty.getPrivateKey(), rsaCipherProperty.getPublicKey(), classLoader, rsaCipherProperty.getPrivatePath(), rsaCipherProperty.getPublicPath());
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
                return encryptObjectSection(data, this.getPublicKey());
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
                return decryptObjectSection(data, this.getPrivateKey());
            } catch (Exception e) {
                throw new CipherException(e);
            }
        }
    }

}

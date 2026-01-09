package cn.coderxiaoc.cipher;

import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.property.RSACipherProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.security.PrivateKey;
import java.security.PublicKey;

public class RSACipherDefault extends RSACipherAbstract {
    private static final String ALGORITHM = "rsa";

    public RSACipherDefault(RSACipherProperty rsaCipherProperty, ClassLoader classLoader) {
        super();
        Assert.notNull(rsaCipherProperty, "rsa property is null");

        if (hasKeyPair(rsaCipherProperty)) {
            this.loadSecretKey(
                    rsaCipherProperty.getPrivateKey(),
                    rsaCipherProperty.getPublicKey(),
                    classLoader,
                    rsaCipherProperty.getPrivatePath(),
                    rsaCipherProperty.getPublicPath()
            );
        }
    }

    @Override
    public String algorithm() {
        return ALGORITHM;
    }

    @Override
    public byte[] encrypt(byte[] data, String publicKey) {
        Assert.isTrue(data != null && data.length > 0, "data is empty");
        try {
            PublicKey key = StringUtils.hasText(publicKey) ? restorePublicKey(publicKey) : getPublicKey();
            if (key == null) {
                throw new IllegalStateException("public key is empty");
            }
            return encryptObjectSection(data, key);
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

    @Override
    public byte[] decrypt(String data, String privateKey) {
        Assert.isTrue(StringUtils.hasText(data), "data is empty");
        try {
            PrivateKey key = StringUtils.hasText(privateKey) ? restorePrivateKey(privateKey) : getPrivateKey();
            if (key == null) {
                throw new IllegalStateException("private key is empty");
            }
            return decryptObjectSection(data, key);
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

    private boolean hasKeyPair(RSACipherProperty property) {
        boolean hasPrivate = StringUtils.hasText(property.getPrivateKey()) || StringUtils.hasText(property.getPrivatePath());
        boolean hasPublic = StringUtils.hasText(property.getPublicKey()) || StringUtils.hasText(property.getPublicPath());
        return hasPrivate && hasPublic;
    }
}

package cn.coderxiaoc.cipher;

import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.property.AESCipherProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;

public class AESCipherDefault extends AESCipherAbstract {
    private static final String ALGORITHM = "aes";
    private final AESCipherProperty aesCipherProperty;

    public AESCipherDefault(AESCipherProperty aesCipherProperty) {
        super();
        Assert.notNull(aesCipherProperty, "aes property is null");
        this.aesCipherProperty = aesCipherProperty;
    }

    @Override
    public String algorithm() {
        return ALGORITHM;
    }

    @Override
    public byte[] encrypt(byte[] data, String secretKey) {
        Assert.isTrue(data != null && data.length > 0, "data is empty");
        try {
            return encryptObject(data, resolveSecretKey(secretKey));
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

    @Override
    public byte[] decrypt(String data, String secretKey) {
        Assert.isTrue(StringUtils.hasText(data), "data is empty");
        try {
            return decryptObject(data, resolveSecretKey(secretKey));
        } catch (Exception e) {
            throw new CipherException(e);
        }
    }

    private SecretKeySpec resolveSecretKey(String secretKey) {
        String key = StringUtils.hasText(secretKey) ? secretKey : aesCipherProperty.getSecretKey();
        Assert.isTrue(StringUtils.hasText(key), "secretKey is empty");
        return getSecretKey(key);
    }
}

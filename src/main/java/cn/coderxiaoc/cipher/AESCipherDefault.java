package cn.coderxiaoc.cipher;

import cn.coderxiaoc.exception.CreateSecretKeyException;
import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.property.AESCipherProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;

public class AESCipherDefault extends AESCipherAbstract {
    private final SecretKeySpec secretKey;
    private final AESCipherProperty aesCipherProperty;
    public AESCipherDefault(AESCipherProperty aesCipherProperty) {
        super();
        Assert.notNull(aesCipherProperty, "aes property is null");
        Assert.isTrue(!StringUtils.isEmpty(aesCipherProperty.getSecretKey()), "secretKey is empty");
        try {
            this.secretKey = this.getSecretKey(aesCipherProperty.getSecretKey());
            this.aesCipherProperty = aesCipherProperty;
        } catch (Exception e) {
            throw new CreateSecretKeyException(e);
        }
    }
    @Override
    public byte[] encrypt(byte[] data, String secretKey) {
        Assert.isTrue(data != null || data.length == 0, "data is empty");
        try {
            if (StringUtils.isEmpty(secretKey)) {
                return this.encryptObject(data, this.secretKey);
            }
            return this.encryptObject(data, this.getSecretKey(secretKey));
        }catch (Exception e) {
            throw new CipherException(e);
        }
    }

    @Override
    public byte[] decrypt(String data, String secretKey) {
        Assert.isTrue(data != null || data.length() == 0, "data is empty");
        try {
            if (StringUtils.isEmpty(secretKey)) {
                return this.decryptObject(data, this.secretKey);
            }
            return this.decryptObject(data, this.getSecretKey(secretKey));
        }catch (Exception e) {
            throw new CipherException(e);
        }
    }
}

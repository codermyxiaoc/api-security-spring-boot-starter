package cn.coderxiaoc.encrypt;

import cn.coderxiaoc.exception.CreateSecretKeyException;
import cn.coderxiaoc.exception.CipherException;
import cn.coderxiaoc.property.AESProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;

public class AESCipherDefault extends AESCipherAbstract {
    private final SecretKeySpec secretKey;
    private final AESProperty aesProperty;
    public AESCipherDefault(AESProperty aesProperty) {
        super();
        Assert.notNull(aesProperty, "aes property is null");
        Assert.isTrue(!StringUtils.isEmpty(aesProperty.getSecretKey()), "secretKey is empty");
        try {
            this.secretKey = this.getSecretKey(aesProperty.getSecretKey());
            this.aesProperty = aesProperty;
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

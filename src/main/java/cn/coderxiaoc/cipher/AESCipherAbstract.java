package cn.coderxiaoc.cipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public abstract class AESCipherAbstract implements Cipher {
    public byte[] encryptObject(byte[] data, SecretKeySpec secretKey) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encode(encryptedBytes);
    }

    public byte[] decryptObject(String data, SecretKeySpec secretKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(data);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return decryptedBytes;
    }

    public SecretKeySpec getSecretKey(String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "AES");
    }
}

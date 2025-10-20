package cn.coderxiaoc.cipher;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public abstract class AESCipherAbstract implements Cipher {
    public byte[] encryptObject(byte[] data, SecretKeySpec secretKey) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encode(encryptedBytes);
    }

    public byte[] decryptObject(String data, SecretKeySpec secretKey) throws Exception {
        byte[] encryptedBytes = Base64.getDecoder().decode(data);
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return decryptedBytes;
    }

    public SecretKeySpec getSecretKey(String secretKey) throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        secureRandom.setSeed(secretKey.getBytes());
        keyGen.init(128, secureRandom);
        SecretKey secret = keyGen.generateKey();
        return new SecretKeySpec(secret.getEncoded(), "AES");
    }
}

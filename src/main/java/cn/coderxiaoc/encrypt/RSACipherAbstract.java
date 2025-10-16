package cn.coderxiaoc.encrypt;

import jdk.nashorn.internal.ir.CallNode;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public abstract class RSACipherAbstract implements Cipher {
    public byte[] encryptObjectSection(byte[] data, PublicKey publicKey) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
        cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);

        int blockSize = 117;
        byte[] result = decryptAndDecrypt(data, cipher, blockSize);
        return Base64.getEncoder().encode(result);
    }

    public byte[] decryptObjectSection(String data, PrivateKey privateKey) throws Exception {
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("RSA");
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);

        int blockSize = 128;
        byte[] result = Base64.getDecoder().decode(data.getBytes());
        return decryptAndDecrypt(result, cipher, blockSize);
    }

    private byte[]decryptAndDecrypt(byte[] data, javax.crypto.Cipher cipher, int blockSize) throws Exception {
        int dataLength = data.length;
        List<byte[]> decryptedBlocks = new ArrayList<>();
        for (int i = 0; i < dataLength; i += blockSize) {
            int endIndex = Math.min(dataLength, i + blockSize);
            byte[] block = cipher.doFinal(data, i, endIndex - i);
            decryptedBlocks.add(block);
        }


        int totalLength = decryptedBlocks.stream().mapToInt(b -> b.length).sum();
        byte[] resultDate = new byte[totalLength];
        int position = 0;
        for (byte[] block : decryptedBlocks) {
            System.arraycopy(block, 0, resultDate, position, block.length);
            position += block.length;
        }
        return resultDate;
    }

    public  PrivateKey restorePrivateKey(String privateKeyStr) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    public  PublicKey restorePublicKey(String publicKeyStr) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }
}

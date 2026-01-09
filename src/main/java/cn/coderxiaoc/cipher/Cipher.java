package cn.coderxiaoc.cipher;

public interface Cipher {
    String algorithm();
    byte[] encrypt(byte[] data, String secretKey);
    byte[] decrypt(String data, String secretKey);
}

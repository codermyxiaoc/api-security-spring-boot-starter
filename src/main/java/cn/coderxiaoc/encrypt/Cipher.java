package cn.coderxiaoc.encrypt;

public interface Cipher {
    byte[] encrypt(byte[]  data, String secretKey);
    byte[] decrypt(String data, String secretKey);
}

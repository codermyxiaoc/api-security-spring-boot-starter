package cn.coderxiaoc.signature;

public interface Signature {
    String algorithm();
    String sign(String params);
    boolean verify(String sing, String params);

}

package cn.coderxiaoc.signature;

import cn.coderxiaoc.exception.MD5Exception;
import cn.coderxiaoc.property.MD5SignatureProperty;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MD5SignatureAbstractDefault implements Signature {
    private final MD5SignatureProperty signatureProperty;
    public MD5SignatureAbstractDefault(MD5SignatureProperty signatureProperty) {
        Assert.notNull(signatureProperty, "signatureProperty can not be null");
        Assert.notNull(signatureProperty.getSecretKey(), "signatureProperty key can not be null");
        this.signatureProperty = signatureProperty;
    }
    private String appendKey(String params) {
        return params  + signatureProperty.getDelimiter() + signatureProperty.getSecretKey();
    }
    private  String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String var = appendKey(input);
            byte[] digest = md.digest(var.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (Exception e) {
            throw new MD5Exception("MD5 sign error");
        }
    }
    @Override
    public String sign(String params) {
        return md5(params);
    }

    @Override
    public boolean verify(String sing, String params) {
        return md5(params).equals(sing);
    }
}

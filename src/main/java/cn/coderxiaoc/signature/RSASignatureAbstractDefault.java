package cn.coderxiaoc.signature;

import cn.coderxiaoc.property.RSASignatureProperty;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class RSASignatureAbstractDefault extends RSASignatureAbstract {
    private final RSASignatureProperty rsaSignatureProperty;
    private final ClassLoader classLoader;
    public RSASignatureAbstractDefault(RSASignatureProperty rsaSignatureProperty, ClassLoader classLoader) {
        super();
        Assert.notNull(rsaSignatureProperty, "rsa property is null");
        Assert.isTrue(StringUtils.hasText(rsaSignatureProperty.getPrivateKey()) || StringUtils.hasText(rsaSignatureProperty.getPrivatePath()), "private key is null");
        Assert.isTrue(StringUtils.hasText(rsaSignatureProperty.getPublicKey()) || StringUtils.hasText(rsaSignatureProperty.getPublicPath()), "public key is null");
        this.rsaSignatureProperty = rsaSignatureProperty;
        this.classLoader = classLoader;
        loadSecretKey(rsaSignatureProperty.getPrivateKey(), rsaSignatureProperty.getPublicKey(), classLoader, rsaSignatureProperty.getPrivatePath(), rsaSignatureProperty.getPublicPath());
    }

    @Override
    public String sign(String params) {
        try {
            byte[] bytes = signWithByte(params.getBytes(), getPrivateKey());
            return new String(bytes);
        } catch (Exception e) {
            throw new RuntimeException( e);
        }

    }

    @Override
    public boolean verify(String sing, String params) {
        try {
           return verifyWithByte(params.getBytes(), sing.getBytes(), getPublicKey());
        } catch (Exception e) {
            throw new RuntimeException( e);
        }
    }
}

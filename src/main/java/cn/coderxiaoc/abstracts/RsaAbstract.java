package cn.coderxiaoc.abstracts;

import cn.coderxiaoc.exception.ReadSecretKeyException;
import cn.coderxiaoc.utils.RSAUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public abstract class RsaAbstract {
    private  PublicKey publicKey;
    private  PrivateKey privateKey;
    public PrivateKey restorePrivateKey(String privateKeyStr) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        return keyFactory.generatePrivate(privateKeySpec);
    }

    public PublicKey restorePublicKey(String publicKeyStr) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        return keyFactory.generatePublic(publicKeySpec);
    }
    protected PublicKey getPublicKey() {
        return publicKey;
    }
    protected PrivateKey getPrivateKey() {
        return privateKey;
    }
    protected void loadSecretKey(String privateKeyStr, String publicKeyStr, ClassLoader classLoader, String privatePath, String publicPath) {
        if (StringUtils.hasText(privateKeyStr)) {
            try {
                this.privateKey = restorePrivateKey( privateKeyStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            InputStream resourceAsStream = null;
            try {

                resourceAsStream =  classLoader.getResourceAsStream(privatePath);
                byte[] bytes = IOUtils.toByteArray(resourceAsStream);
                this.privateKey = restorePrivateKey(RSAUtil.readPrivateKeyFromByte(bytes));

            } catch (Exception e) {
                throw new ReadSecretKeyException(e);
            } finally {
                if (resourceAsStream != null) {
                    IOUtils.closeQuietly(resourceAsStream);
                }
            }
        }
        if (StringUtils.hasText(publicKeyStr)){
            try {
                this.publicKey = restorePublicKey(publicKeyStr);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = classLoader.getResourceAsStream(publicPath);
                byte[] bytes = IOUtils.toByteArray(resourceAsStream);
                this.publicKey = restorePublicKey(RSAUtil.readPublicKeyFromByte(bytes));
            } catch (Exception e) {
                throw new ReadSecretKeyException( e);
            } finally {
                if (resourceAsStream != null) {
                    IOUtils.closeQuietly(resourceAsStream);
                }
            }
        }

    }
}

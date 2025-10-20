package cn.coderxiaoc.signature;

import cn.coderxiaoc.abstracts.RsaAbstract;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public abstract class RSASignatureAbstract extends RsaAbstract implements Signature {
    public byte[] signWithByte(byte[] singStr, PrivateKey privateKey) throws Exception {
        java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(singStr);
        byte[] sign = signature.sign();
        return Base64.getEncoder().encode(sign);
    }

    public boolean verifyWithByte(byte[] verifyBytes, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        signatureBytes = Base64.getDecoder().decode(signatureBytes);

        java.security.Signature signature = java.security.Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(verifyBytes);
        return signature.verify(signatureBytes);
    }
}

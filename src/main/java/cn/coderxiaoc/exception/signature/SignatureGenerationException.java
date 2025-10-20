package cn.coderxiaoc.exception.signature;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureGenerationException extends SignatureVerificationBaseException {
    public SignatureGenerationException(String message) {
        super(message);
    }
    public SignatureGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}

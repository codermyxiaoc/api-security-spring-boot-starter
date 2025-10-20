package cn.coderxiaoc.exception.verify;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureVerificationUnknownException extends SignatureVerificationBaseException {
    public SignatureVerificationUnknownException(String message, Throwable cause) {
        super(message, cause);
    }
}

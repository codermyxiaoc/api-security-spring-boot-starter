package cn.coderxiaoc.exception.verify;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureMissingException extends SignatureVerificationBaseException {
    public SignatureMissingException(String message) {
        super(message);
    }
}

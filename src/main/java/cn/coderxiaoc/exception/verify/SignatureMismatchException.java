package cn.coderxiaoc.exception.verify;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureMismatchException extends SignatureVerificationBaseException {
    public SignatureMismatchException(String message) {
        super(message);
    }
}

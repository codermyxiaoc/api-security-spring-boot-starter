package cn.coderxiaoc.exception.verify;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class InvalidSignatureFieldException extends SignatureVerificationBaseException {
    public InvalidSignatureFieldException(String message) {
        super(message);
    }
}

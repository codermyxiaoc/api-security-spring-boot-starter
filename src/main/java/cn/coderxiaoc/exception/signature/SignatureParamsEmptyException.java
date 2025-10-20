package cn.coderxiaoc.exception.signature;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureParamsEmptyException extends SignatureVerificationBaseException {
    public SignatureParamsEmptyException(String message) {
        super(message);
    }
}

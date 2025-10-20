package cn.coderxiaoc.exception.signature;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class SignatureParamsParseException extends SignatureVerificationBaseException {
    public SignatureParamsParseException(String message) {
        super(message);
    }
    public SignatureParamsParseException(String message, Throwable cause) {
        super(message, cause);
    }
}

package cn.coderxiaoc.exception.verify;

import cn.coderxiaoc.exception.SignatureVerificationBaseException;

public class PreventDuplicateException extends SignatureVerificationBaseException {
    public PreventDuplicateException(String message) {
        super(message);
    }
}

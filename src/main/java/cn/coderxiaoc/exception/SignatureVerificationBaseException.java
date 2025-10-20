package cn.coderxiaoc.exception;

public abstract class SignatureVerificationBaseException extends RuntimeException {
    public SignatureVerificationBaseException(String message) {
        super(message);
    }

    public SignatureVerificationBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

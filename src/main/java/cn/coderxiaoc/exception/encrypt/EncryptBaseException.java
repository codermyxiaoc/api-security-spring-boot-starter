package cn.coderxiaoc.exception.encrypt;

public class EncryptBaseException extends RuntimeException {
    public EncryptBaseException(String message) {
        super(message);
    }
    public EncryptBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

package cn.coderxiaoc.exception.decrypt;

public class DecryptBaseException extends RuntimeException {
    public DecryptBaseException(String message) {
        super(message);
    }
    public DecryptBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

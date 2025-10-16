package cn.coderxiaoc.exception.decrypt;

public class DecryptFailedException extends DecryptBaseException {
    public DecryptFailedException(String message) {
        super(message);
    }
    public DecryptFailedException(String message, Throwable cause) {
        super(message,  cause);
    }
}

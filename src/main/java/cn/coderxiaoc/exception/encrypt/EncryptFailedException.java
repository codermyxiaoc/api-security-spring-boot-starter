package cn.coderxiaoc.exception.encrypt;

public class EncryptFailedException extends EncryptBaseException{
    public EncryptFailedException(String message, Throwable cause) {
        super(message, cause);
    }
    public EncryptFailedException(String message) {
        super(message);
    }
}

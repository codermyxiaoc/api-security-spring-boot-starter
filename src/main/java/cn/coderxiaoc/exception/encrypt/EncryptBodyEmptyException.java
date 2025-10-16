package cn.coderxiaoc.exception.encrypt;

import cn.coderxiaoc.exception.CipherException;

public class EncryptBodyEmptyException extends EncryptBaseException {
    public EncryptBodyEmptyException(String message) {
        super(message);
    }

}

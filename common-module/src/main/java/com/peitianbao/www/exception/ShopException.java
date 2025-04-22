package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class ShopException extends ServiceException {

    public ShopException() {
    }

    public ShopException(String message) {
        super(message);
    }

    public ShopException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShopException(Throwable cause) {
        super(cause);
    }
}

package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class BlogException extends ServiceException {
    public BlogException() {
    }

    public BlogException(String message) {
        super(message);
    }

    public BlogException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlogException(Throwable cause) {
        super(cause);
    }
}

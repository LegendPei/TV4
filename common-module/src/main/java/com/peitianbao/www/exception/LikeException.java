package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class LikeException extends ServiceException {
    public LikeException() {
    }

    public LikeException(String message) {
        super(message);
    }

    public LikeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LikeException(Throwable cause) {
        super(cause);
    }
}

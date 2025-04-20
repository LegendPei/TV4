package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class FollowException extends ServiceException {
    public FollowException() {
    }

    public FollowException(String message) {
        super(message);
    }

    public FollowException(String message, Throwable cause) {
        super(message, cause);
    }

    public FollowException(Throwable cause) {
        super(cause);
    }
}

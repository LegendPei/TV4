package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class CommentException extends ServiceException {
    public CommentException() {
    }

    public CommentException(String message) {
        super(message);
    }

    public CommentException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommentException(Throwable cause) {
        super(cause);
    }
}

package com.peitianbao.www.exception;

/**
 * @author leg
 */
public class VoucherException extends ServiceException {
    public VoucherException() {
        super();
    }

    public VoucherException(String message) {
        super(message);
    }

    public VoucherException(String message, Throwable cause) {
        super(message, cause);
    }

    public VoucherException(Throwable cause) {
        super(cause);
    }
}

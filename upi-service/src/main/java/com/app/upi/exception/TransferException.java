package com.app.upi.exception;

import java.io.Serial;

/**
 * Custom exception for UPI transfer business logic failures.
 */
public class TransferException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TransferException(String message) {
        super(message);
    }


    public TransferException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.app.upi.exception;

import java.io.Serial;

public class AccountException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 2L;

    public AccountException(String message) {
        super(message);
    }


    public AccountException(String message, Throwable cause) {
        super(message, cause);
    }
}
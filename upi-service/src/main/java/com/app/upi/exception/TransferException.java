package com.app.upi.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransferException extends RuntimeException {

    private final HttpStatus status;

    public TransferException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TransferException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }
}
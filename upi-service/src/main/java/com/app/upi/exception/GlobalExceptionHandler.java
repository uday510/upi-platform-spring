package com.app.upi.exception;

import com.app.upi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<ErrorResponse> handleAccountException(
            AccountException ex,
            HttpServletRequest request
    ) {
        log.error("Account error: {}", ex.getMessage());

        // Map "already exists" to 409 CONFLICT, otherwise 400 BAD REQUEST
        HttpStatus status = ex.getMessage().contains("exists")
                ? HttpStatus.CONFLICT
                : HttpStatus.BAD_REQUEST;

        return buildResponse(status, ex.getMessage(), request);
    }

    @ExceptionHandler(TransferException.class)
    public ResponseEntity<ErrorResponse> handleTransferException(
            TransferException ex,
            HttpServletRequest request
    ) {
        log.error("Transfer failed: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // Catch-all for unexpected system errors
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error occurred", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.",
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(error, status);
    }
}
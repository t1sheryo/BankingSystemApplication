package com.bankingsystem.app.exceptionHandler;

import com.bankingsystem.app.customException.LimitUpdateNotAllowedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LimitUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleLimitUpdateNotAllowedException(
            LimitUpdateNotAllowedException ex) {

        ErrorResponse error = ErrorResponse.create(
                ex,
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(error);
    }
}
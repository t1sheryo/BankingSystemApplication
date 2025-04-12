package com.bankingsystem.app.exceptionHandler;

import com.bankingsystem.app.customException.LimitUpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// FIXME: можно сделать чтобы все ошибки тут обрабатывались,
//  чтобы при ошибках приложение не ложилось

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid input: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.create(
                e,
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Invalid input: " + e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        log.error("Invalid state: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.create(
                e,
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Resource not found: " + e.getMessage());
    }

    @ExceptionHandler(LimitUpdateNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleLimitUpdateNotAllowedException(
            LimitUpdateNotAllowedException e) {

        log.warn("LimitUpdateNotAllowedException: {}", e.getMessage());

        ErrorResponse error = ErrorResponse.create(
                e,
                HttpStatus.FORBIDDEN,
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNullBody(HttpMessageNotReadableException ex) {
        ErrorResponse error = ErrorResponse.create(
            ex,
            HttpStatus.BAD_REQUEST,
            "Request body is required and cannot be null"
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDataAccessException(DataAccessException e) {
        log.error("Database error: {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.create(
                e,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Database error occurred");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);

        ErrorResponse error = ErrorResponse.create(
                e,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }
}
package com.bankingsystem.app.exceptionHandler;

import com.bankingsystem.app.customException.LimitUpdateNotAllowedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// FIXME: можно сделать чтобы все ошибки тут обрабатывались,
//  чтобы при ошибках приложение не ложилось

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        log.error("No argument: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        List<String> errors = new ArrayList<>();
        errors.add("Argument '" + ex.getParameterName() + "' is required");
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.error("Type mismatch: {}", e.getMessage());

        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid input: " + e.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

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
                .body(error.toString());
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
                .body(error.toString());
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        log.error("Validation error: {}", ex.getMessage());

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("errors", errors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
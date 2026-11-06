
package com.ravikanth.asset_management.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle invalid request data and business-rule violations
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // Handle requests that are not valid for the current application state
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // Handle invalid request body fields
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse("Request validation failed");

        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    // Handle constraint violations outside request body validation
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        return createErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // Build a consistent error response
    private ResponseEntity<ErrorResponse> createErrorResponse(
            HttpStatus status,
            String message
    ) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                status.getReasonPhrase().toUpperCase().replace(' ', '_'),
                message
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }

    // Standard error response returned by the API
    public record ErrorResponse(
            int status,
            String error,
            String message
    ) {
    }
}

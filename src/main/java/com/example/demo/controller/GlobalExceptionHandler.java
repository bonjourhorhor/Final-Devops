package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler that converts exceptions into proper HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle entity-not-found errors → 404.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage();
        // If the message implies "not found", return 404
        if (msg != null && (msg.contains("not found") || msg.contains("No profiles found"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Not Found", "message", msg));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "message", msg != null ? msg : "An unexpected error occurred"));
    }

    /**
     * Handle validation errors → 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Bad Request", "message", ex.getMessage()));
    }

    /**
     * Handle all other exceptions → 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error",
                             "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error"));
    }
}

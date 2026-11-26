package com.example.taskmanager.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom runtime exception for API errors.
 */
@Getter
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}
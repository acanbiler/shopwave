package com.shopwave.exception;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
} 
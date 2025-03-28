package com.bankingsystem.app.customExceptions;

public class LimitUpdateNotAllowedException extends RuntimeException {
    public LimitUpdateNotAllowedException(String message) {
        super(message);
    }
}

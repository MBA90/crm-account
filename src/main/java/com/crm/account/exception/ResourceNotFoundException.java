package com.crm.account.exception;

/**
 * Thrown when a requested account does not exist (or has been erased).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

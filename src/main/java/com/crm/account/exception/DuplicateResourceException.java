package com.crm.account.exception;

/**
 * Thrown when creating/updating an account would violate the
 * (registration_no, country) uniqueness constraint.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

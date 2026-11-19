package com.manager.exception;

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException() {
        super("An account with this email already exists.");
    }
}

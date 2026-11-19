package com.manager.exception;

public class DuplicateContactException extends RuntimeException {

    public DuplicateContactException() {
        super("A contact with this phone number already exists.");
    }
}

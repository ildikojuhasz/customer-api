package com.example.customerapi.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A customer with email already exists: " + email);
    }
}

package com.spring.security.web.exception;

public class UserIdentityNotFoundException extends RuntimeException {

    public UserIdentityNotFoundException(String message) {
        super(message);
    }
}

package com.mauamott.loginuser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuthExceptions {

    public static class BadCredentialsException extends ResponseStatusException {
        public BadCredentialsException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }
        public static class InvalidTotpException extends ResponseStatusException {
        public InvalidTotpException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }

}

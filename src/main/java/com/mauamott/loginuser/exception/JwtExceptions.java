package com.mauamott.loginuser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class JwtExceptions {

    public static class BadTokenException extends ResponseStatusException {
        public BadTokenException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }

    public static class NoTokenWasFoundException extends ResponseStatusException {
        public NoTokenWasFoundException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }

    public static class InvalidAuthException extends ResponseStatusException {
        public InvalidAuthException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }
}

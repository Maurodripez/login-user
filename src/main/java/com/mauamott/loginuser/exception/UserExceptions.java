package com.mauamott.loginuser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserExceptions {


    public static class UsernameExistException extends RuntimeException {
        public UsernameExistException(String message) {
            super(message);
        }
    }

    public static class EmailExistException extends RuntimeException {
        public EmailExistException(String message) {
            super(message);
        }
    }

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) {
            super(message);
        }
    }
    public static class TokenNotValidException extends RuntimeException {
        public TokenNotValidException(String message) {
            super(message);
        }
    }
    public static class InvalidRoleException extends ResponseStatusException {
        public InvalidRoleException(String message) {
            super(HttpStatus.BAD_REQUEST, message);
        }
    }
    public static class DuplicateUsernameException extends ResponseStatusException {
        public DuplicateUsernameException(String message) {
            super(HttpStatus.CONFLICT, message);
        }
    }

    public static class DuplicateEmailException extends ResponseStatusException {
        public DuplicateEmailException(String message) {
            super(HttpStatus.CONFLICT, message);
        }
    }

    public static class IncorrectPasswordException extends ResponseStatusException {
        public IncorrectPasswordException(String message) {
            super(HttpStatus.UNAUTHORIZED, message);
        }
    }

}
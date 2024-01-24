package com.mauamott.loginuser.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AuditExceptions {

    public static class ErrorAuditLogException extends ResponseStatusException {
        public ErrorAuditLogException(String message) {
            super(HttpStatus.INTERNAL_SERVER_ERROR, message);
        }
    }
}

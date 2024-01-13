package com.mauamott.loginuser.handlers;

import reactor.core.publisher.Mono;

public interface AuditHandler {

    Mono<Boolean> saveAuditLog(String userId,String eventType);
}

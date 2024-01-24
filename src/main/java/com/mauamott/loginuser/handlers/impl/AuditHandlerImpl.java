package com.mauamott.loginuser.handlers.impl;

import com.mauamott.loginuser.documents.AuditLog;
import com.mauamott.loginuser.handlers.AuditHandler;
import com.mauamott.loginuser.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
@Component
@Slf4j
public class AuditHandlerImpl implements AuditHandler {
    private final AuditLogRepository auditLogRepository;

    public AuditHandlerImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public Mono<Boolean> saveAuditLog(String userId, String eventType) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(userId);
        auditLog.setEventType(eventType);
        auditLog.setTimestamp(LocalDateTime.now());
        log.info("audit log :" + auditLog.toString());
        return auditLogRepository.save(auditLog)
                .flatMap(savedLog -> Mono.just(true))
                .onErrorResume(error -> Mono.just(false));
    }

}

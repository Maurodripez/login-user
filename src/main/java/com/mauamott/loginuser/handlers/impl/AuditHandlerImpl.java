package com.mauamott.loginuser.handlers.impl;

import com.mauamott.loginuser.documents.AuditEvent;
import com.mauamott.loginuser.documents.AuditLog;
import static com.mauamott.loginuser.exception.AuditExceptions.ErrorAuditLogException;
import com.mauamott.loginuser.handlers.AuditHandler;
import com.mauamott.loginuser.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuditHandlerImpl implements AuditHandler {
    private final AuditLogRepository auditLogRepository;

    @Override
    public Mono<Boolean> saveAuditLog(String userId, String eventType) {
        AuditEvent newAuditEvent = new AuditEvent();
        newAuditEvent.setEventType(eventType);
        newAuditEvent.setTimestamp(LocalDateTime.now());

        return auditLogRepository.findByUserId(userId)
                .flatMap(existingAuditLog -> {
                    existingAuditLog.getAuditEvents().add(newAuditEvent);
                    return auditLogRepository.save(existingAuditLog)
                            .map(savedLog -> true);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    AuditLog auditLog = new AuditLog();
                    auditLog.setUserId(userId);
                    auditLog.setAuditEvents(new ArrayList<>(Collections.singletonList(newAuditEvent)));
                    return auditLogRepository.save(auditLog)
                            .map(savedLog -> true);
                }))
                .onErrorResume(error -> {
                    log.error("Error saving audit log", error);
                    throw new ErrorAuditLogException("Error saving audit log");
                });
    }






}

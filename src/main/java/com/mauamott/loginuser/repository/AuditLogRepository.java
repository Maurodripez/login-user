package com.mauamott.loginuser.repository;

import com.mauamott.loginuser.documents.AuditLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface AuditLogRepository extends ReactiveMongoRepository <AuditLog, String>{
    Mono<AuditLog> findByUserId(String userId);
}

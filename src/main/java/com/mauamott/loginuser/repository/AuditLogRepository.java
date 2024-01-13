package com.mauamott.loginuser.repository;

import com.mauamott.loginuser.documents.AuditLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuditLogRepository extends ReactiveMongoRepository <AuditLog, String>{
}

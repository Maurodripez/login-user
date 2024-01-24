package com.mauamott.loginuser.documents;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data

@Document(collection = "audit_logs")
public class AuditLog {

    private String id;
    private String userId;
    private String eventType;
    private LocalDateTime timestamp;
    private List<AuditEvent> auditEvents;
}

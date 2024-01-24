package com.mauamott.loginuser.documents;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditEvent {

    private String eventType;
    private LocalDateTime timestamp;
}
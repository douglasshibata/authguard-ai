package com.authguard.ai.service;

import com.authguard.ai.model.AuditLog;
import com.authguard.ai.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void log(String account, String type, boolean success, Double risk, String ip, String ua, String msg) {
        AuditLog log = AuditLog.builder()
                .account(account)
                .eventType(type)
                .success(success)
                .riskScore(risk)
                .ipAddress(ip)
                .userAgent(ua)
                .message(msg)
                .createdAt(LocalDateTime.now())
                .build();
        repository.save(log);
    }
}

package com.avira.platformservice.mvp.dto;

import java.time.Instant;
import java.util.Map;

import com.avira.platformservice.mvp.ApplicationStatus;
import com.avira.platformservice.mvp.AuthMode;

public record ApplicationResponse(
        String appId,
        String tenantId,
        String name,
        String domain,
        AuthMode authMode,
        ApplicationStatus status,
        Map<String, Object> config,
        Instant createdAt
) {
}

package com.avira.projectservice.application.dto;

import com.avira.projectservice.application.enums.ApplicationKind;
import com.avira.projectservice.application.enums.ApplicationStatus;

import java.time.Instant;

public record ApplicationResponse(
        String id,
        String tenantId,
        String name,
        String domain,
        ApplicationKind kind,
        String description,
        ApplicationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}


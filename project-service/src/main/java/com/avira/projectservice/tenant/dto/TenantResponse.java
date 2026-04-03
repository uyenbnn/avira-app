package com.avira.projectservice.tenant.dto;

import com.avira.projectservice.tenant.enums.TenantStatus;
import java.time.Instant;

public record TenantResponse(
        String id,
        String name,
        String description,
        String ownerId,
        TenantStatus status,
        Integer maxUsers,
        Instant createdAt,
        Instant updatedAt
) {
}


package com.avira.platformservice.mvp.dto;

import java.time.Instant;

import com.avira.platformservice.mvp.IdentityMode;
import com.avira.platformservice.mvp.TenantStatus;

public record TenantResponse(
        String tenantId,
        String name,
        String contactEmail,
        IdentityMode identityMode,
        TenantStatus status,
        Instant createdAt
) {
}

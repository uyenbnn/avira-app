package com.avira.iamservice.clientservice.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.avira.iamservice.clientservice.domain.IdentityMode;

public record TenantRealmConfigResponse(
        UUID tenantId,
        IdentityMode identityMode,
        String realmName,
        boolean dedicatedRealmApproved,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

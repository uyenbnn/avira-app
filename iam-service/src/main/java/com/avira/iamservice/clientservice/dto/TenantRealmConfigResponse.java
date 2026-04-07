package com.avira.iamservice.clientservice.dto;

import com.avira.iamservice.clientservice.domain.IdentityMode;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TenantRealmConfigResponse(
        UUID tenantId,
        IdentityMode identityMode,
        String realmName,
        OffsetDateTime createdAt
) {
}


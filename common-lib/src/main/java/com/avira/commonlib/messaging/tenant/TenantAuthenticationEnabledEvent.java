package com.avira.commonlib.messaging.tenant;

public record TenantAuthenticationEnabledEvent(
        String tenantId,
        String name,
        String ownerId
) {
}


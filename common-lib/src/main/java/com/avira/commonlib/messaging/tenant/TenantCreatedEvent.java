package com.avira.commonlib.messaging.tenant;

public record TenantCreatedEvent(
        String tenantId,
        String name,
        String description,
        String ownerId,
        Integer maxUsers,
        Boolean authenticationEnabled
) {
}


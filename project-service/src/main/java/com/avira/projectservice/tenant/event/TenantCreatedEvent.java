package com.avira.projectservice.tenant.event;

public record TenantCreatedEvent(
        String tenantId,
        String name,
        String description,
        String ownerId,
        Integer maxUsers
) {
}


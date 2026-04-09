package com.avira.commonlib.messaging.application;

public record ApplicationCreatedEvent(
        String applicationId,
        String tenantId,
        String ownerId,
        String name,
        String domain,
        String kind
) {
}


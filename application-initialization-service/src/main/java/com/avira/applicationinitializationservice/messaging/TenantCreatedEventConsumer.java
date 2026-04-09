package com.avira.applicationinitializationservice.messaging;

import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.TenantDomainActions;
import com.avira.commonlib.messaging.EventConsumer;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.tenant.TenantCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedEventConsumer implements EventConsumer<TenantCreatedEvent> {

    private final KeycloakInitializationService keycloakInitializationService;

    @Override
    public String domain() {
        return EventTopics.TENANT_DOMAIN;
    }

    @Override
    public String action() {
        return TenantDomainActions.CREATED;
    }

    @Override
    public Class<TenantCreatedEvent> payloadType() {
        return TenantCreatedEvent.class;
    }

    @Override
    public void handle(EventEnvelope<TenantCreatedEvent> event) {
        TenantCreatedEvent payload = event.payload();
        if (!Boolean.TRUE.equals(payload.authenticationEnabled())) {
            log.info("Skipping tenant Keycloak initialization for tenantId={} because authenticationEnabled=false", payload.tenantId());
            return;
        }

        log.info("Handling tenant-domain created event for tenantId={} eventId={}", payload.tenantId(), event.eventId());
        keycloakInitializationService.initializeTenantKeycloak(payload);
    }
}


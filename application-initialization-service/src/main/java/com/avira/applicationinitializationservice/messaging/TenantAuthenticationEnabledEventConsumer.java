package com.avira.applicationinitializationservice.messaging;

import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.TenantDomainActions;
import com.avira.commonlib.messaging.EventConsumer;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.tenant.TenantAuthenticationEnabledEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantAuthenticationEnabledEventConsumer implements EventConsumer<TenantAuthenticationEnabledEvent> {

    private final KeycloakInitializationService keycloakInitializationService;

    @Override
    public String domain() {
        return EventTopics.TENANT_DOMAIN;
    }

    @Override
    public String action() {
        return TenantDomainActions.AUTHENTICATION_ENABLED;
    }

    @Override
    public Class<TenantAuthenticationEnabledEvent> payloadType() {
        return TenantAuthenticationEnabledEvent.class;
    }

    @Override
    public void handle(EventEnvelope<TenantAuthenticationEnabledEvent> event) {
        TenantAuthenticationEnabledEvent payload = event.payload();
        log.info("Handling tenant-domain authentication-enabled event for tenantId={} eventId={}",
                payload.tenantId(), event.eventId());
        keycloakInitializationService.initializeTenantKeycloak(payload);
    }
}


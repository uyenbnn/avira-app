package com.avira.applicationinitializationservice.messaging;

import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.tenant.TenantCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantCreatedEventConsumerTest {

    @Mock
    private KeycloakInitializationService keycloakInitializationService;

    @InjectMocks
    private TenantCreatedEventConsumer consumer;

    @Test
    void shouldInitializeTenantKeycloakWhenAuthenticationEnabled() {
        TenantCreatedEvent payload = new TenantCreatedEvent("tenant-1", "Tenant One", "desc", "owner", 100, true);
        EventEnvelope<TenantCreatedEvent> event = new EventEnvelope<>(
                "event-1",
                "tx-1",
                "tenant-domain",
                "created",
                "project-service",
                "tenant-1",
                payload,
                Instant.now(),
                Map.of()
        );

        consumer.handle(event);

        verify(keycloakInitializationService).initializeTenantKeycloak(payload);
    }

    @Test
    void shouldSkipTenantKeycloakWhenAuthenticationDisabled() {
        TenantCreatedEvent payload = new TenantCreatedEvent("tenant-1", "Tenant One", "desc", "owner", 100, false);
        EventEnvelope<TenantCreatedEvent> event = new EventEnvelope<>(
                "event-1",
                "tx-1",
                "tenant-domain",
                "created",
                "project-service",
                "tenant-1",
                payload,
                Instant.now(),
                Map.of()
        );

        consumer.handle(event);

        verify(keycloakInitializationService, never()).initializeTenantKeycloak(payload);
    }
}


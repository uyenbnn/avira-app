package com.avira.applicationinitializationservice.messaging;

import com.avira.applicationinitializationservice.service.KeycloakInitializationService;
import com.avira.commonlib.messaging.EventEnvelope;
import com.avira.commonlib.messaging.tenant.TenantAuthenticationEnabledEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantAuthenticationEnabledEventConsumerTest {

    @Mock
    private KeycloakInitializationService keycloakInitializationService;

    @InjectMocks
    private TenantAuthenticationEnabledEventConsumer consumer;

    @Test
    void shouldInitializeTenantKeycloakOnAuthenticationEnabledEvent() {
        TenantAuthenticationEnabledEvent payload = new TenantAuthenticationEnabledEvent("tenant-1", "Tenant One", "owner-1");
        EventEnvelope<TenantAuthenticationEnabledEvent> event = new EventEnvelope<>(
                "event-2",
                "tx-2",
                "tenant-domain",
                "authentication_enabled",
                "project-service",
                "tenant-1",
                payload,
                Instant.now(),
                Map.of()
        );

        consumer.handle(event);

        verify(keycloakInitializationService).initializeTenantKeycloak(payload);
    }
}


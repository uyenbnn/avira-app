package com.avira.applicationservice.authentication.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.avira.applicationservice.authentication.AuthMode;
import com.avira.applicationservice.authentication.strategy.CustomJwtAuthenticationHandlerStrategy;
import com.avira.applicationservice.authentication.strategy.KeycloakAuthenticationHandlerStrategy;
import com.avira.applicationservice.authentication.strategy.PassthroughAuthenticationHandlerStrategy;

class AuthenticationStrategyRouterTest {

    @Test
    void shouldResolveKeycloakStrategy() {
        AuthenticationStrategyRouter router = new AuthenticationStrategyRouter(List.of(
                new KeycloakAuthenticationHandlerStrategy(),
                new CustomJwtAuthenticationHandlerStrategy(),
                new PassthroughAuthenticationHandlerStrategy()
        ));

        assertEquals(AuthMode.KEYCLOAK, router.resolve(AuthMode.KEYCLOAK).supports());
    }

    @Test
    void shouldFailForUnsupportedStrategy() {
        AuthenticationStrategyRouter router = new AuthenticationStrategyRouter(List.of(
                new KeycloakAuthenticationHandlerStrategy()
        ));

        assertThrows(IllegalArgumentException.class, () -> router.resolve(AuthMode.PASSTHROUGH));
    }
}

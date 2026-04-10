package com.avira.iamservice.authenticationservice.service;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import com.avira.iamservice.authenticationservice.integration.AuthenticationProvider;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private AuthenticationProvider authenticationProvider;

    @Test
    void shouldBuildApplicationTokenPayloadOnLogin() {
        TokenResponse providerResponse = new TokenResponse(
                "access",
                "refresh",
                "Bearer",
                300L,
                "KEYCLOAK",
                "tenant-1",
                "app-1",
                Map.of()
        );

        when(authenticationProvider.login("tenant-1", "john", "pw")).thenReturn(providerResponse);
        when(authenticationProvider.introspect("tenant-1", "access")).thenReturn(true);
        when(authenticationProvider.userInfo("tenant-1", "access")).thenReturn(Map.of(
                "sub", "user-1",
                "realm_access", Map.of("roles", List.of("ADMIN", "USER"))
        ));

        AuthenticationService service = new AuthenticationService(authenticationProvider);
        TokenResponse response = service.login("tenant-1", "app-1", "john", "pw");

        assertEquals("tenant-1", response.tenantId());
        assertEquals("app-1", response.appId());
        assertEquals("user-1", response.applicationTokenPayload().get("userId"));
        assertTrue(response.applicationTokenPayload().containsKey("roles"));
    }

    @Test
    void shouldCallProviderLogoutWithTenant() {
        AuthenticationService service = new AuthenticationService(authenticationProvider);
        service.logout("tenant-2", "refresh");
        verify(authenticationProvider).logout("tenant-2", "refresh");
    }
}

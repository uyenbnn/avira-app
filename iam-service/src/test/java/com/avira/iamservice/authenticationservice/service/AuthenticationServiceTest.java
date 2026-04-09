package com.avira.iamservice.authenticationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.avira.commonlib.client.KeycloakTokenWebClient;
import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private KeycloakTokenWebClient keycloakTokenWebClient;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldMapLoginResponse() {
        when(keycloakTokenWebClient.login("admin", "admin")).thenReturn(Map.of(
                "access_token", "access-token",
                "refresh_token", "refresh-token",
                "token_type", "Bearer",
                "expires_in", 300,
                "refresh_expires_in", 1800,
                "scope", "openid profile"
        ));

        TokenResponse response = authenticationService.login("admin", "admin");

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(300L);
        assertThat(response.refreshExpiresIn()).isEqualTo(1800L);
        assertThat(response.scope()).isEqualTo("openid profile");
    }
}



package com.avira.authenticationservice.config;

import com.avira.commonlib.config.properties.KeycloakAdminProperties;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminConfig {

    private final KeycloakAdminProperties keycloakAdminProperties;

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakAdminProperties.getServerUrl())
                .realm(keycloakAdminProperties.getRealm())
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(keycloakAdminProperties.getClientId())
                .username(keycloakAdminProperties.getUsername())
                .password(keycloakAdminProperties.getPassword())
                .build();
    }
}


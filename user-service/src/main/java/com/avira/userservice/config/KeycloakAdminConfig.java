package com.avira.userservice.config;

import com.avira.commonlib.config.properties.KeycloakProperties;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakAdminConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean
    public Keycloak keycloakMaster() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.getSync().getServerUrl())
                .realm(keycloakProperties.getAdmin().getRealm())
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();
    }
}

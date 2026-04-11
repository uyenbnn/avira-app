package com.avira.iamservice.realm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "iam.init.keycloak")
public record KeycloakInitProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("http://localhost:8080") String baseUrl,
        @DefaultValue("master") String adminRealm,
        @DefaultValue("admin-cli") String adminClientId,
        @DefaultValue("admin") String adminUsername,
        @DefaultValue("admin") String adminPassword
) {
}
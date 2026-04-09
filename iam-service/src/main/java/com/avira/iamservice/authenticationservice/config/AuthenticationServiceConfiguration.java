package com.avira.iamservice.authenticationservice.config;

import com.avira.commonlib.client.KeycloakTokenWebClient;
import com.avira.iamservice.authenticationservice.config.properties.KeycloakAuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(KeycloakAuthProperties.class)
public class AuthenticationServiceConfiguration {

    @Bean
    public KeycloakTokenWebClient keycloakTokenWebClient(KeycloakAuthProperties properties) {
        return new KeycloakTokenWebClient(
                WebClient.builder(),
                properties.getTokenUrl(),
                properties.getLogoutUrl(),
                properties.getClientId(),
                properties.getClientSecret()
        );
    }
}


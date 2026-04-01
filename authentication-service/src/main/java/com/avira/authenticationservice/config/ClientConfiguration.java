package com.avira.authenticationservice.config;

import com.avira.commonlib.client.KeycloakTokenWebClient;
import com.avira.commonlib.config.properties.KeycloakAuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ClientConfiguration {

    private final KeycloakAuthProperties keycloakAuthProperties;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }


    @Bean
    public KeycloakTokenWebClient keycloakTokenWebClient(WebClient.Builder webClientBuilder) {
        return new KeycloakTokenWebClient(
                webClientBuilder,
                keycloakAuthProperties.getTokenUrl(),
                keycloakAuthProperties.getLogoutUrl(),
                keycloakAuthProperties.getClientId(),
                keycloakAuthProperties.getClientSecret());
    }
}

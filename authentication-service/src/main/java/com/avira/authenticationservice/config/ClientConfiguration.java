package com.avira.authenticationservice.config;

import com.avira.commonlib.client.KeycloakTokenWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfiguration {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }


    @Bean
    public KeycloakTokenWebClient keycloakTokenWebClient(
            WebClient.Builder webClientBuilder,
            @Value("${keycloak.auth.token-url:http://localhost:8080/realms/avira/protocol/openid-connect/token}") String tokenUrl,
            @Value("${keycloak.auth.client-id:avira-user-client}") String clientId,
            @Value("${keycloak.auth.client-secret:}") String clientSecret) {
        return new KeycloakTokenWebClient(webClientBuilder, tokenUrl, clientId, clientSecret);
    }
}

package com.avira.iamservice.initservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakHttpClientConfig {

    @Bean
    public WebClient keycloakAdminWebClient() {
        return WebClient.builder().build();
    }

    @Bean
    public RestTemplate keycloakRestTemplate() {
        return new RestTemplate();
    }
}

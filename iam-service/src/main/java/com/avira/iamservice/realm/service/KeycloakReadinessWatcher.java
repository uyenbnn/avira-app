package com.avira.iamservice.realm.service;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.avira.iamservice.realm.config.KeycloakInitProperties;

@Component
public class KeycloakReadinessWatcher {

    private static final int MAX_ATTEMPTS = 10;
    private static final long INITIAL_DELAY_SECONDS = 2L;
    private static final long MAX_DELAY_SECONDS = 30L;

    private final RestClient restClient;

    public KeycloakReadinessWatcher(RestClient.Builder restClientBuilder, KeycloakInitProperties keycloakInitProperties) {
        this.restClient = restClientBuilder
                .baseUrl(trimTrailingSlash(keycloakInitProperties.baseUrl()))
                .build();
    }

    public void awaitReady() {
        long delaySeconds = INITIAL_DELAY_SECONDS;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                restClient.get()
                        .uri("/health/ready")
                        .retrieve()
                        .toBodilessEntity();
                return;
            } catch (RuntimeException ex) {
                lastError = ex;
                if (attempt == MAX_ATTEMPTS) {
                    break;
                }
                sleep(delaySeconds);
                delaySeconds = Math.min(delaySeconds * 2, MAX_DELAY_SECONDS);
            }
        }

        throw new IllegalStateException("Keycloak did not become ready after " + MAX_ATTEMPTS + " attempts", lastError);
    }

    private static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for Keycloak readiness", ex);
        }
    }

    private static String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("iam.init.keycloak.base-url must not be blank");
        }
        String trimmed = value.trim();
        if (trimmed.endsWith("/")) {
            return trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
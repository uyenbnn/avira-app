package com.avira.iamservice.realm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.avira.iamservice.realm.dto.SharedRealmInitResponse;

@Component
@ConditionalOnProperty(prefix = "iam.init.keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SharedRealmBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(SharedRealmBootstrap.class);

    private static final int MAX_ATTEMPTS = 10;
    private static final long INITIAL_DELAY_SECONDS = 2L;
    private static final long MAX_DELAY_SECONDS = 30L;

    private final KeycloakReadinessWatcher keycloakReadinessWatcher;
    private final RealmProvisioningService realmProvisioningService;

    private volatile SharedRealmInitStatus status = SharedRealmInitStatus.PENDING;

    public SharedRealmBootstrap(
            KeycloakReadinessWatcher keycloakReadinessWatcher,
            RealmProvisioningService realmProvisioningService
    ) {
        this.keycloakReadinessWatcher = keycloakReadinessWatcher;
        this.realmProvisioningService = realmProvisioningService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void bootstrapSharedRealm() {
        status = SharedRealmInitStatus.INITIALIZING;
        try {
            keycloakReadinessWatcher.awaitReady();
            retryInitSharedRealm();
            status = SharedRealmInitStatus.READY;
        } catch (RuntimeException ex) {
            status = SharedRealmInitStatus.FAILED;
            throw ex;
        }
    }

    public SharedRealmInitStatus status() {
        return status;
    }

    private void retryInitSharedRealm() {
        long delaySeconds = INITIAL_DELAY_SECONDS;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                SharedRealmInitResponse response = realmProvisioningService.initSharedRealm();
                LOGGER.info("Shared realm bootstrap completed with status={} realm={}", response.status(), response.realm());
                return;
            } catch (RuntimeException ex) {
                lastError = ex;
                LOGGER.warn("Shared realm bootstrap attempt {} failed: {}", attempt, ex.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    break;
                }
                sleep(delaySeconds);
                delaySeconds = Math.min(delaySeconds * 2, MAX_DELAY_SECONDS);
            }
        }

        throw new IllegalStateException("Shared realm bootstrap failed after " + MAX_ATTEMPTS + " attempts", lastError);
    }

    private static void sleep(long seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while bootstrapping shared realm", ex);
        }
    }
}
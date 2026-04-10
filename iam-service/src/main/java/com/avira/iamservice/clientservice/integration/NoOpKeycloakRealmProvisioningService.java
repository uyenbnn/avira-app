package com.avira.iamservice.clientservice.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "iam.init.keycloak", name = "enabled", havingValue = "false")
public class NoOpKeycloakRealmProvisioningService implements KeycloakRealmProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(NoOpKeycloakRealmProvisioningService.class);

    @Override
    public void ensureRealmExists(String realmName) {
        log.info("No-op provisioning for realm '{}' (replace with Keycloak Admin API integration).", realmName);
    }
}

package com.avira.iamservice.clientservice.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoOpKeycloakRealmProvisioningService implements KeycloakRealmProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(NoOpKeycloakRealmProvisioningService.class);

    @Override
    public void ensureRealmExists(String realmName) {
        log.info("Realm bootstrap requested for '{}'. Replace NoOp implementation with real Keycloak Admin API integration.", realmName);
    }
}


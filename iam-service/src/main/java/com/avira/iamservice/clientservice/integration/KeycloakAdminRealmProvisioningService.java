package com.avira.iamservice.clientservice.integration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.avira.iamservice.initservice.service.KeycloakProvisionService;

@Service
@ConditionalOnProperty(prefix = "iam.init.keycloak", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KeycloakAdminRealmProvisioningService implements KeycloakRealmProvisioningService {

    private final KeycloakProvisionService keycloakProvisionService;

    public KeycloakAdminRealmProvisioningService(KeycloakProvisionService keycloakProvisionService) {
        this.keycloakProvisionService = keycloakProvisionService;
    }

    @Override
    public void ensureRealmExists(String realmName) {
        keycloakProvisionService.ensureRealmProvisioned(realmName);
    }
}

package com.avira.iamservice.initservice.init;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.avira.iamservice.initservice.config.KeycloakInitProperties;
import com.avira.iamservice.initservice.service.KeycloakProvisionService;

@Component
public class InitRunner implements ApplicationRunner {

    private final KeycloakProvisionService keycloakProvisionService;
    private final KeycloakInitProperties keycloakInitProperties;

    public InitRunner(KeycloakProvisionService keycloakProvisionService, KeycloakInitProperties keycloakInitProperties) {
        this.keycloakProvisionService = keycloakProvisionService;
        this.keycloakInitProperties = keycloakInitProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!keycloakInitProperties.isEnabled()) {
            return;
        }

        keycloakProvisionService.provisionSharedRealm();
        keycloakProvisionService.provisionTenantRealm(keycloakInitProperties.getDefaultTenantId());
    }
}

package com.avira.iamservice.initservice.service;

import com.avira.iamservice.initservice.config.IamRealmProperties;
import com.avira.iamservice.initservice.integration.KeycloakRealmProvisioningService;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedRealmBootstrap {

    @Bean
    public ApplicationRunner sharedRealmBootstrapRunner(
            IamRealmProperties iamRealmProperties,
            KeycloakRealmProvisioningService keycloakRealmProvisioningService) {
        return args -> keycloakRealmProvisioningService.ensureRealmExists(iamRealmProperties.getSharedName());
    }
}



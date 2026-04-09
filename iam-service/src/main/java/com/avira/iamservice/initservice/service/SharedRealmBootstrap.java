package com.avira.iamservice.initservice.service;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.integration.KeycloakRealmProvisioningService;

@Configuration
public class SharedRealmBootstrap {

    @Bean
    public ApplicationRunner sharedRealmBootstrapRunner(
            IamRealmProperties iamRealmProperties,
            KeycloakRealmProvisioningService keycloakRealmProvisioningService
    ) {
        return args -> keycloakRealmProvisioningService.ensureRealmExists(iamRealmProperties.getSharedName());
    }
}

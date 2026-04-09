package com.avira.iamservice.clientservice.service;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.integration.KeycloakRealmProvisioningService;
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


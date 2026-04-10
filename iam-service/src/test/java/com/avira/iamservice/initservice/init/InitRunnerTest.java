package com.avira.iamservice.initservice.init;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.DefaultApplicationArguments;

import com.avira.iamservice.initservice.config.KeycloakInitProperties;
import com.avira.iamservice.initservice.service.KeycloakProvisionService;

class InitRunnerTest {

    @Test
    void shouldProvisionSharedAndDefaultDedicatedRealmWhenEnabled() throws Exception {
        KeycloakProvisionService provisionService = Mockito.mock(KeycloakProvisionService.class);
        KeycloakInitProperties properties = new KeycloakInitProperties();
        properties.setEnabled(true);
        properties.setDefaultTenantId("default-saas");

        InitRunner initRunner = new InitRunner(provisionService, properties);
        initRunner.run(new DefaultApplicationArguments(new String[]{}));

        verify(provisionService).provisionSharedRealm();
        verify(provisionService).provisionTenantRealm("default-saas");
    }

    @Test
    void shouldSkipProvisioningWhenDisabled() throws Exception {
        KeycloakProvisionService provisionService = Mockito.mock(KeycloakProvisionService.class);
        KeycloakInitProperties properties = new KeycloakInitProperties();
        properties.setEnabled(false);

        InitRunner initRunner = new InitRunner(provisionService, properties);
        initRunner.run(new DefaultApplicationArguments(new String[]{}));

        verify(provisionService, never()).provisionSharedRealm();
        verify(provisionService, never()).provisionTenantRealm(Mockito.anyString());
    }
}
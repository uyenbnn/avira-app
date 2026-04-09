package com.avira.iamservice.clientservice.integration;

public interface KeycloakRealmProvisioningService {

    void ensureRealmExists(String realmName);
}


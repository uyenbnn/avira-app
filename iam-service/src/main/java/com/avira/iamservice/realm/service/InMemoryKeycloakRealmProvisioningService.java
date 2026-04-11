package com.avira.iamservice.realm.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class InMemoryKeycloakRealmProvisioningService implements KeycloakRealmProvisioningService {
    private final Set<String> initializedRealms = ConcurrentHashMap.newKeySet();
    private final Set<String> provisionedTenants = ConcurrentHashMap.newKeySet();

    @Override
    public boolean initSharedRealm(String realmName) {
        return initializedRealms.add(realmName);
    }

    @Override
    public TenantClientProvisionResult provisionTenantClient(String tenantId, String realmName) {
        String key = realmName + ":" + tenantId;
        boolean created = provisionedTenants.add(key);
        String clientId = "tenant-" + tenantId + "-client";
        return new TenantClientProvisionResult(clientId, created ? "PROVISIONED" : "ALREADY_EXISTS");
    }
}

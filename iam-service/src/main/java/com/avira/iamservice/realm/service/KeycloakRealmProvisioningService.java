package com.avira.iamservice.realm.service;

public interface KeycloakRealmProvisioningService {
    boolean initSharedRealm(String realmName);

    TenantClientProvisionResult provisionTenantClient(String tenantId, String realmName);
}

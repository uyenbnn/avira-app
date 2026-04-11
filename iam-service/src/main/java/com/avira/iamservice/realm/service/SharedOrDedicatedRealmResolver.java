package com.avira.iamservice.realm.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.avira.iamservice.realm.IdentityMode;
import com.avira.iamservice.realm.TenantRealmConfig;
import com.avira.iamservice.realm.config.IamRealmProperties;

@Component
public class SharedOrDedicatedRealmResolver implements RealmResolver {
    private final TenantRealmConfigStore tenantRealmConfigStore;
    private final IamRealmProperties realmProperties;

    public SharedOrDedicatedRealmResolver(TenantRealmConfigStore tenantRealmConfigStore, IamRealmProperties realmProperties) {
        this.tenantRealmConfigStore = tenantRealmConfigStore;
        this.realmProperties = realmProperties;
    }

    @Override
    public String resolveRealm(String tenantId) {
        try {
            UUID.fromString(tenantId);
        } catch (IllegalArgumentException ex) {
            return realmProperties.dedicatedPrefix() + tenantId;
        }

        TenantRealmConfig config = tenantRealmConfigStore.findByTenantId(tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Missing tenant realm config for tenant: " + tenantId));

        if (config.identityMode() == IdentityMode.SHARED_REALM) {
            return realmProperties.sharedName();
        }

        return realmProperties.dedicatedPrefix() + tenantId;
    }
}

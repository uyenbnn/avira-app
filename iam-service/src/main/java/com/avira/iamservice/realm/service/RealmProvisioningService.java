package com.avira.iamservice.realm.service;

import org.springframework.stereotype.Service;

import com.avira.iamservice.realm.IdentityMode;
import com.avira.iamservice.realm.TenantRealmConfig;
import com.avira.iamservice.realm.config.IamRealmProperties;
import com.avira.iamservice.realm.dto.RealmResolutionResponse;
import com.avira.iamservice.realm.dto.SharedRealmInitResponse;
import com.avira.iamservice.realm.dto.TenantProvisionRequest;
import com.avira.iamservice.realm.dto.TenantProvisionResponse;
import com.avira.iamservice.realm.dto.TenantRealmConfigRequest;
import com.avira.iamservice.realm.dto.TenantRealmConfigResponse;

@Service
public class RealmProvisioningService {
    private final IamRealmProperties realmProperties;
    private final KeycloakRealmProvisioningService keycloakRealmProvisioningService;
    private final TenantRealmConfigStore tenantRealmConfigStore;
    private final RealmResolver realmResolver;

    public RealmProvisioningService(
            IamRealmProperties realmProperties,
            KeycloakRealmProvisioningService keycloakRealmProvisioningService,
            TenantRealmConfigStore tenantRealmConfigStore,
            RealmResolver realmResolver
    ) {
        this.realmProperties = realmProperties;
        this.keycloakRealmProvisioningService = keycloakRealmProvisioningService;
        this.tenantRealmConfigStore = tenantRealmConfigStore;
        this.realmResolver = realmResolver;
    }

    public SharedRealmInitResponse initSharedRealm() {
        boolean created = keycloakRealmProvisioningService.initSharedRealm(realmProperties.sharedName());
        return new SharedRealmInitResponse(realmProperties.sharedName(), created ? "INITIALIZED" : "ALREADY_EXISTS");
    }

    public TenantProvisionResponse provisionTenant(String tenantId, TenantProvisionRequest request) {
        if (!tenantId.equals(request.tenantId())) {
            throw new IllegalArgumentException("Tenant id mismatch between path and payload");
        }
        enforceSharedRealm(request.identityMode());

        tenantRealmConfigStore.upsert(new TenantRealmConfig(tenantId, IdentityMode.SHARED_REALM, false));

        String realm = realmResolver.resolveRealm(tenantId);
        TenantClientProvisionResult provisionResult = keycloakRealmProvisioningService.provisionTenantClient(tenantId, realm);

        return new TenantProvisionResponse(tenantId, realm, provisionResult.keycloakClientId(), provisionResult.status());
    }

    public TenantRealmConfigResponse upsertTenantRealmConfig(TenantRealmConfigRequest request) {
        enforceSharedRealm(request.identityMode());

        TenantRealmConfig stored = tenantRealmConfigStore.upsert(
                new TenantRealmConfig(request.tenantId(), IdentityMode.SHARED_REALM, false)
        );

        return new TenantRealmConfigResponse(stored.tenantId(), stored.identityMode(), stored.dedicatedRealmApproved());
    }

    public RealmResolutionResponse resolveRealm(String tenantId) {
        String realm = realmResolver.resolveRealm(tenantId);
        return new RealmResolutionResponse(tenantId, realm, IdentityMode.SHARED_REALM);
    }

    private static void enforceSharedRealm(IdentityMode identityMode) {
        if (identityMode != IdentityMode.SHARED_REALM) {
            throw new IllegalArgumentException("MVP only supports SHARED_REALM");
        }
    }
}

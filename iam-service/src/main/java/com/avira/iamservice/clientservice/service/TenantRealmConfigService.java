package com.avira.iamservice.clientservice.service;

import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.initservice.integration.KeycloakRealmProvisioningService;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantRealmConfigService {

    private final TenantRealmConfigRepository tenantRealmConfigRepository;
    private final KeycloakRealmProvisioningService keycloakRealmProvisioningService;

    public TenantRealmConfigService(TenantRealmConfigRepository tenantRealmConfigRepository,
                                    KeycloakRealmProvisioningService keycloakRealmProvisioningService) {
        this.tenantRealmConfigRepository = tenantRealmConfigRepository;
        this.keycloakRealmProvisioningService = keycloakRealmProvisioningService;
    }

    @Transactional
    public TenantRealmConfig upsert(UUID tenantId, IdentityMode identityMode, String realmName) {
        TenantRealmConfig config = new TenantRealmConfig(tenantId, identityMode, realmName, OffsetDateTime.now());
        if (identityMode == IdentityMode.DEDICATED_REALM) {
            keycloakRealmProvisioningService.ensureRealmExists(realmName);
        }
        return tenantRealmConfigRepository.save(config);
    }
}




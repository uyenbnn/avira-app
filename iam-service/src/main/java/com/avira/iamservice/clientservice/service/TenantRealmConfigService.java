package com.avira.iamservice.clientservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.dto.UpsertTenantRealmConfigRequest;
import com.avira.iamservice.clientservice.exception.InvalidTenantConfigException;
import com.avira.iamservice.clientservice.integration.KeycloakRealmProvisioningService;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;
import com.avira.iamservice.clientservice.util.RealmNameUtil;

@Service
public class TenantRealmConfigService {

    private final TenantRealmConfigRepository tenantRealmConfigRepository;
    private final IamRealmProperties iamRealmProperties;
    private final KeycloakRealmProvisioningService keycloakRealmProvisioningService;

    public TenantRealmConfigService(
            TenantRealmConfigRepository tenantRealmConfigRepository,
            IamRealmProperties iamRealmProperties,
            KeycloakRealmProvisioningService keycloakRealmProvisioningService
    ) {
        this.tenantRealmConfigRepository = tenantRealmConfigRepository;
        this.iamRealmProperties = iamRealmProperties;
        this.keycloakRealmProvisioningService = keycloakRealmProvisioningService;
    }

    @Transactional
    public TenantRealmConfig upsert(UpsertTenantRealmConfigRequest request) {
        validateRequest(request);

        UUID tenantId = UUID.fromString(request.getTenantId());
        String realmName = resolveRealmName(request);

        TenantRealmConfig config = tenantRealmConfigRepository
                .findById(tenantId)
                .orElseGet(() -> new TenantRealmConfig(
                        tenantId,
                        request.getIdentityMode(),
                        realmName,
                        request.isDedicatedRealmApproved()
                ));

        config.setIdentityMode(request.getIdentityMode());
        config.setRealmName(realmName);
        config.setDedicatedRealmApproved(request.isDedicatedRealmApproved());

        if (request.getIdentityMode() == IdentityMode.DEDICATED_REALM) {
            keycloakRealmProvisioningService.ensureRealmExists(realmName);
        }

        return tenantRealmConfigRepository.save(config);
    }

    private void validateRequest(UpsertTenantRealmConfigRequest request) {
        if (request.getTenantId() == null || request.getTenantId().isBlank()) {
            throw new InvalidTenantConfigException("tenantId is required");
        }
        if (request.getIdentityMode() == null) {
            throw new InvalidTenantConfigException("identityMode is required");
        }
        if (request.getIdentityMode() == IdentityMode.DEDICATED_REALM && !request.isDedicatedRealmApproved()) {
            throw new InvalidTenantConfigException("Dedicated realm requires manual approval");
        }
    }

    private String resolveRealmName(UpsertTenantRealmConfigRequest request) {
        if (request.getIdentityMode() == IdentityMode.SHARED_REALM) {
            return iamRealmProperties.getSharedName();
        }

        if (request.getRealmName() != null && !request.getRealmName().isBlank()) {
            return request.getRealmName();
        }

        return RealmNameUtil.defaultDedicatedRealm(iamRealmProperties.getDedicatedPrefix(), request.getTenantId());
    }
}

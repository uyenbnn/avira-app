package com.avira.iamservice.clientservice.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;

@Service
public class DefaultRealmResolver implements RealmResolver {

    private final TenantRealmConfigRepository tenantRealmConfigRepository;
    private final IamRealmProperties iamRealmProperties;

    public DefaultRealmResolver(
            TenantRealmConfigRepository tenantRealmConfigRepository,
            IamRealmProperties iamRealmProperties
    ) {
        this.tenantRealmConfigRepository = tenantRealmConfigRepository;
        this.iamRealmProperties = iamRealmProperties;
    }

    @Override
    public String resolveRealm(String tenantId) {
        TenantRealmConfig config = tenantRealmConfigRepository.findById(UUID.fromString(tenantId))
                .orElseThrow(() -> new IllegalArgumentException("Tenant config not found for tenantId: " + tenantId));

        if (config.getIdentityMode() == IdentityMode.SHARED_REALM) {
            return iamRealmProperties.getSharedName();
        }

        return config.getRealmName();
    }
}

package com.avira.iamservice.initservice.realm;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.avira.iamservice.clientservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;
import com.avira.iamservice.clientservice.util.RealmNameUtil;

@Component
public class SharedOrDedicatedRealmResolver implements RealmResolver {

    private final TenantRealmConfigRepository tenantRealmConfigRepository;
    private final IamRealmProperties iamRealmProperties;

    public SharedOrDedicatedRealmResolver(
            TenantRealmConfigRepository tenantRealmConfigRepository,
            IamRealmProperties iamRealmProperties
    ) {
        this.tenantRealmConfigRepository = tenantRealmConfigRepository;
        this.iamRealmProperties = iamRealmProperties;
    }

    @Override
    public String resolveRealm(String tenantId) {
        UUID parsedTenantId;
        try {
            parsedTenantId = UUID.fromString(tenantId);
        } catch (IllegalArgumentException ignored) {
            return RealmNameUtil.defaultDedicatedRealm(iamRealmProperties.getDedicatedPrefix(), tenantId);
        }

        TenantRealmConfig config = tenantRealmConfigRepository.findById(parsedTenantId)
                .orElseThrow(() -> new IllegalArgumentException("Tenant config not found for tenantId: " + tenantId));

        if (config.getIdentityMode() == IdentityMode.SHARED_REALM) {
            return iamRealmProperties.getSharedName();
        }

        return RealmNameUtil.defaultDedicatedRealm(iamRealmProperties.getDedicatedPrefix(), tenantId);
    }
}

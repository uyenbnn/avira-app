package com.avira.iamservice.clientservice.service;

import com.avira.commonlib.exception.NotFoundException;
import com.avira.iamservice.initservice.config.IamRealmProperties;
import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.repository.TenantRealmConfigRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DefaultRealmResolver implements RealmResolver {

    private final TenantRealmConfigRepository tenantRealmConfigRepository;
    private final IamRealmProperties iamRealmProperties;

    public DefaultRealmResolver(TenantRealmConfigRepository tenantRealmConfigRepository, IamRealmProperties iamRealmProperties) {
        this.tenantRealmConfigRepository = tenantRealmConfigRepository;
        this.iamRealmProperties = iamRealmProperties;
    }

    @Override
    public String resolveRealm(String tenantId) {
        UUID tenantUuid = UUID.fromString(tenantId);
        TenantRealmConfig config = tenantRealmConfigRepository.findById(tenantUuid)
                .orElseThrow(() -> new NotFoundException("Tenant realm config not found: " + tenantId));

        if (config.getIdentityMode() == IdentityMode.SHARED_REALM) {
            return iamRealmProperties.getSharedName();
        }

        if (StringUtils.hasText(config.getRealmName())) {
            return config.getRealmName();
        }

        throw new IllegalStateException("Dedicated realm tenant requires a realm name");
    }
}



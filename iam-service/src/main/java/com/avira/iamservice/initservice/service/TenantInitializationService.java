package com.avira.iamservice.initservice.service;

import com.avira.iamservice.clientservice.domain.IdentityMode;
import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.dto.TenantRealmConfigResponse;
import com.avira.iamservice.clientservice.dto.UpsertTenantRealmConfigRequest;
import com.avira.iamservice.clientservice.service.TenantRealmConfigService;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TenantInitializationService {

    private final TenantRealmConfigService tenantRealmConfigService;

    public TenantInitializationService(TenantRealmConfigService tenantRealmConfigService) {
        this.tenantRealmConfigService = tenantRealmConfigService;
    }

    public TenantRealmConfigResponse initializeTenantRealm(UpsertTenantRealmConfigRequest request) {
        String realmName = resolveRealmName(request);
        TenantRealmConfig config = tenantRealmConfigService.upsert(
                UUID.fromString(request.getTenantId()),
                request.getIdentityMode(),
                realmName
        );

        return new TenantRealmConfigResponse(
                config.getTenantId(),
                config.getIdentityMode(),
                config.getRealmName(),
                config.getCreatedAt()
        );
    }

    private String resolveRealmName(UpsertTenantRealmConfigRequest request) {
        if (request.getIdentityMode() == IdentityMode.SHARED_REALM) {
            return "avira-platform";
        }
        if (!StringUtils.hasText(request.getRealmName())) {
            return "tenant_" + request.getTenantId();
        }
        return request.getRealmName();
    }
}


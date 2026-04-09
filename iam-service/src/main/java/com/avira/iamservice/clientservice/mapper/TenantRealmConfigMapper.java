package com.avira.iamservice.clientservice.mapper;

import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import com.avira.iamservice.clientservice.dto.TenantRealmConfigResponse;

public final class TenantRealmConfigMapper {

    private TenantRealmConfigMapper() {
    }

    public static TenantRealmConfigResponse toResponse(TenantRealmConfig config) {
        return new TenantRealmConfigResponse(
                config.getTenantId(),
                config.getIdentityMode(),
                config.getRealmName(),
                config.isDedicatedRealmApproved(),
                config.getCreatedAt(),
                config.getUpdatedAt()
        );
    }
}

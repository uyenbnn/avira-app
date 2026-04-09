package com.avira.iamservice.clientservice.dto;

import com.avira.iamservice.clientservice.domain.IdentityMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class UpsertTenantRealmConfigRequest {

    @NotBlank
    private String tenantId;

    @NotNull
    private IdentityMode identityMode;

    private String realmName;

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public IdentityMode getIdentityMode() {
        return identityMode;
    }

    public void setIdentityMode(IdentityMode identityMode) {
        this.identityMode = identityMode;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }
}


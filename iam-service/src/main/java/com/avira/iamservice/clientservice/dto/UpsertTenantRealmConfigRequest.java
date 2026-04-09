package com.avira.iamservice.clientservice.dto;

import com.avira.iamservice.clientservice.domain.IdentityMode;

public class UpsertTenantRealmConfigRequest {

    private String tenantId;
    private IdentityMode identityMode;
    private String realmName;
    private boolean dedicatedRealmApproved;

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

    public boolean isDedicatedRealmApproved() {
        return dedicatedRealmApproved;
    }

    public void setDedicatedRealmApproved(boolean dedicatedRealmApproved) {
        this.dedicatedRealmApproved = dedicatedRealmApproved;
    }
}

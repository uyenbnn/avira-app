package com.avira.iamservice.clientservice.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenant_realm_config")
public class TenantRealmConfig {

    @Id
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private IdentityMode identityMode;

    @Column(nullable = false, length = 128)
    private String realmName;

    @Column(nullable = false)
    private boolean dedicatedRealmApproved;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected TenantRealmConfig() {
    }

    public TenantRealmConfig(
            UUID tenantId,
            IdentityMode identityMode,
            String realmName,
            boolean dedicatedRealmApproved
    ) {
        this.tenantId = tenantId;
        this.identityMode = identityMode;
        this.realmName = realmName;
        this.dedicatedRealmApproved = dedicatedRealmApproved;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getTenantId() {
        return tenantId;
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}

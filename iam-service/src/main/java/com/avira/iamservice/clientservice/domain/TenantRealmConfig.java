package com.avira.iamservice.clientservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

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
    private OffsetDateTime createdAt;

    protected TenantRealmConfig() {
    }

    public TenantRealmConfig(UUID tenantId, IdentityMode identityMode, String realmName, OffsetDateTime createdAt) {
        this.tenantId = tenantId;
        this.identityMode = identityMode;
        this.realmName = realmName;
        this.createdAt = createdAt;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public IdentityMode getIdentityMode() {
        return identityMode;
    }

    public String getRealmName() {
        return realmName;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}


package com.avira.iamservice.realm.service;

import java.util.Optional;

import com.avira.iamservice.realm.TenantRealmConfig;

public interface TenantRealmConfigStore {
    TenantRealmConfig upsert(TenantRealmConfig config);

    Optional<TenantRealmConfig> findByTenantId(String tenantId);
}

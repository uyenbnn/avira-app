package com.avira.iamservice.realm.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.avira.iamservice.realm.TenantRealmConfig;

@Component
public class InMemoryTenantRealmConfigStore implements TenantRealmConfigStore {
    private final Map<String, TenantRealmConfig> store = new ConcurrentHashMap<>();

    @Override
    public TenantRealmConfig upsert(TenantRealmConfig config) {
        store.put(config.tenantId(), config);
        return config;
    }

    @Override
    public Optional<TenantRealmConfig> findByTenantId(String tenantId) {
        return Optional.ofNullable(store.get(tenantId));
    }
}

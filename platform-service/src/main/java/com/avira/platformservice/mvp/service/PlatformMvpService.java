package com.avira.platformservice.mvp.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.avira.platformservice.mvp.ApplicationStatus;
import com.avira.platformservice.mvp.IdentityMode;
import com.avira.platformservice.mvp.TenantStatus;
import com.avira.platformservice.mvp.dto.ApplicationRequest;
import com.avira.platformservice.mvp.dto.ApplicationResponse;
import com.avira.platformservice.mvp.dto.TenantRequest;
import com.avira.platformservice.mvp.dto.TenantResponse;

@Service
public class PlatformMvpService {
    private final Map<String, TenantResponse> tenantStore = new ConcurrentHashMap<>();
    private final Map<String, List<ApplicationResponse>> appStoreByTenant = new ConcurrentHashMap<>();

    public TenantResponse createTenant(TenantRequest request) {
        String tenantId = UUID.randomUUID().toString();
        TenantResponse response = new TenantResponse(
                tenantId,
                request.name(),
                request.contactEmail(),
                IdentityMode.SHARED_REALM,
                TenantStatus.ACTIVE,
                Instant.now()
        );
        tenantStore.put(tenantId, response);
        return response;
    }

    public TenantResponse getTenant(String tenantId) {
        TenantResponse tenant = tenantStore.get(tenantId);
        if (tenant == null) {
            throw new IllegalArgumentException("Tenant not found: " + tenantId);
        }
        return tenant;
    }

    public List<TenantResponse> listTenants() {
        return new ArrayList<>(tenantStore.values());
    }

    public ApplicationResponse createApplication(String tenantId, ApplicationRequest request) {
        getTenant(tenantId);

        ApplicationResponse response = new ApplicationResponse(
                UUID.randomUUID().toString(),
                tenantId,
                request.name(),
                request.domain(),
                request.authMode(),
                ApplicationStatus.ACTIVE,
                request.config() == null ? Map.of() : request.config(),
                Instant.now()
        );

        appStoreByTenant.computeIfAbsent(tenantId, key -> new ArrayList<>()).add(response);
        return response;
    }

    public List<ApplicationResponse> listApplications(String tenantId) {
        getTenant(tenantId);
        return List.copyOf(appStoreByTenant.getOrDefault(tenantId, List.of()));
    }

    public ApplicationResponse getApplication(String tenantId, String appId) {
        return listApplications(tenantId).stream()
                .filter(item -> item.appId().equals(appId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + appId));
    }
}

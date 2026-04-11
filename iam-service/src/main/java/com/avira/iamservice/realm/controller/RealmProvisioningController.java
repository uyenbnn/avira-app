package com.avira.iamservice.realm.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.realm.dto.RealmResolutionResponse;
import com.avira.iamservice.realm.dto.SharedRealmInitResponse;
import com.avira.iamservice.realm.dto.TenantProvisionRequest;
import com.avira.iamservice.realm.dto.TenantProvisionResponse;
import com.avira.iamservice.realm.dto.TenantRealmConfigRequest;
import com.avira.iamservice.realm.dto.TenantRealmConfigResponse;
import com.avira.iamservice.realm.service.RealmProvisioningService;

@RestController
@RequestMapping("/api/iam")
public class RealmProvisioningController {
    private final RealmProvisioningService realmProvisioningService;

    public RealmProvisioningController(RealmProvisioningService realmProvisioningService) {
        this.realmProvisioningService = realmProvisioningService;
    }

    @PostMapping("/internal/init/realms")
    public SharedRealmInitResponse initSharedRealm() {
        return realmProvisioningService.initSharedRealm();
    }

    @PostMapping("/internal/init/tenants/{tenantId}")
    public TenantProvisionResponse provisionTenant(
            @PathVariable String tenantId,
            @RequestBody TenantProvisionRequest request
    ) {
        return realmProvisioningService.provisionTenant(tenantId, request);
    }

    @PostMapping("/client/tenant-realm-configs")
    public TenantRealmConfigResponse upsertTenantRealmConfig(@RequestBody TenantRealmConfigRequest request) {
        return realmProvisioningService.upsertTenantRealmConfig(request);
    }

    @GetMapping("/client/realms/tenants/{tenantId}")
    public RealmResolutionResponse resolveRealm(@PathVariable String tenantId) {
        return realmProvisioningService.resolveRealm(tenantId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }
}

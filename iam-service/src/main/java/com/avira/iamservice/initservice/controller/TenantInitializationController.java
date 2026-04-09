package com.avira.iamservice.initservice.controller;

import com.avira.iamservice.clientservice.dto.TenantRealmConfigResponse;
import com.avira.iamservice.clientservice.dto.UpsertTenantRealmConfigRequest;
import com.avira.iamservice.initservice.service.TenantInitializationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/init")
public class TenantInitializationController {

    private final TenantInitializationService tenantInitializationService;

    public TenantInitializationController(TenantInitializationService tenantInitializationService) {
        this.tenantInitializationService = tenantInitializationService;
    }

    @PostMapping("/tenants")
    @ResponseStatus(HttpStatus.CREATED)
    public TenantRealmConfigResponse initializeTenant(@Valid @RequestBody UpsertTenantRealmConfigRequest request) {
        return tenantInitializationService.initializeTenantRealm(request);
    }
}


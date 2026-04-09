package com.avira.iamservice.clientservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.clientservice.dto.TenantRealmConfigResponse;
import com.avira.iamservice.clientservice.dto.UpsertTenantRealmConfigRequest;
import com.avira.iamservice.clientservice.mapper.TenantRealmConfigMapper;
import com.avira.iamservice.clientservice.service.TenantRealmConfigService;

@RestController
@RequestMapping("/api/iam/client/tenant-realm-configs")
public class TenantRealmConfigController {

    private final TenantRealmConfigService tenantRealmConfigService;

    public TenantRealmConfigController(TenantRealmConfigService tenantRealmConfigService) {
        this.tenantRealmConfigService = tenantRealmConfigService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TenantRealmConfigResponse upsert(@RequestBody UpsertTenantRealmConfigRequest request) {
        return TenantRealmConfigMapper.toResponse(tenantRealmConfigService.upsert(request));
    }
}

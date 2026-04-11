package com.avira.platformservice.mvp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.platformservice.mvp.dto.ApplicationRequest;
import com.avira.platformservice.mvp.dto.ApplicationResponse;
import com.avira.platformservice.mvp.dto.TenantRequest;
import com.avira.platformservice.mvp.dto.TenantResponse;
import com.avira.platformservice.mvp.service.PlatformMvpService;

@RestController
@RequestMapping("/api/platform")
public class PlatformMvpController {
    private final PlatformMvpService platformMvpService;

    public PlatformMvpController(PlatformMvpService platformMvpService) {
        this.platformMvpService = platformMvpService;
    }

    @PostMapping("/tenants")
    public ResponseEntity<TenantResponse> createTenant(@RequestBody TenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformMvpService.createTenant(request));
    }

    @GetMapping("/tenants/{tenantId}")
    public TenantResponse getTenant(
            @PathVariable String tenantId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdFromToken,
            @RequestHeader(value = "X-Platform-Admin", required = false, defaultValue = "false") boolean platformAdmin
    ) {
        enforceTenantOwnership(tenantId, tenantIdFromToken, platformAdmin);
        return platformMvpService.getTenant(tenantId);
    }

    @GetMapping("/tenants")
    public List<TenantResponse> listTenants(
            @RequestHeader(value = "X-Platform-Admin", required = false, defaultValue = "false") boolean platformAdmin
    ) {
        if (!platformAdmin) {
            throw new SecurityException("Only platform admin can list all tenants");
        }
        return platformMvpService.listTenants();
    }

    @PostMapping("/tenants/{tenantId}/applications")
    public ResponseEntity<ApplicationResponse> createApplication(
            @PathVariable String tenantId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdFromToken,
            @RequestHeader(value = "X-Platform-Admin", required = false, defaultValue = "false") boolean platformAdmin,
            @RequestBody ApplicationRequest request
    ) {
        enforceTenantOwnership(tenantId, tenantIdFromToken, platformAdmin);
        return ResponseEntity.status(HttpStatus.CREATED).body(platformMvpService.createApplication(tenantId, request));
    }

    @GetMapping("/tenants/{tenantId}/applications")
    public List<ApplicationResponse> listApplications(
            @PathVariable String tenantId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdFromToken,
            @RequestHeader(value = "X-Platform-Admin", required = false, defaultValue = "false") boolean platformAdmin
    ) {
        enforceTenantOwnership(tenantId, tenantIdFromToken, platformAdmin);
        return platformMvpService.listApplications(tenantId);
    }

    @GetMapping("/tenants/{tenantId}/applications/{appId}")
    public ApplicationResponse getApplication(
            @PathVariable String tenantId,
            @PathVariable String appId,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantIdFromToken,
            @RequestHeader(value = "X-Platform-Admin", required = false, defaultValue = "false") boolean platformAdmin
    ) {
        enforceTenantOwnership(tenantId, tenantIdFromToken, platformAdmin);
        return platformMvpService.getApplication(tenantId, appId);
    }

    private static void enforceTenantOwnership(String tenantId, String tenantIdFromToken, boolean platformAdmin) {
        if (platformAdmin) {
            return;
        }
        if (tenantIdFromToken == null || tenantIdFromToken.isBlank()) {
            throw new SecurityException("Missing tenant context from validated token");
        }
        if (!tenantId.equals(tenantIdFromToken)) {
            throw new SecurityException("Cross-tenant access is not allowed");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", ex.getMessage()));
    }
}

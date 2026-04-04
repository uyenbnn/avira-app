package com.avira.projectservice.application.controller;

import com.avira.projectservice.application.dto.ApplicationResponse;
import com.avira.projectservice.application.dto.CreateApplicationRequest;
import com.avira.projectservice.application.dto.UpdateApplicationRequest;
import com.avira.projectservice.application.enums.ApplicationStatus;
import com.avira.projectservice.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // POST /api/tenants/{tenantId}/applications
    @PostMapping("/tenants/{tenantId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApplicationResponse> create(
            @PathVariable String tenantId,
            @Valid @RequestBody CreateApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String ownerId = extractUsername(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.create(tenantId, request, ownerId));
    }

    // GET /api/tenants/{tenantId}/applications
    @GetMapping("/tenants/{tenantId}/applications")
    public ResponseEntity<Page<ApplicationResponse>> findByTenantId(
            @PathVariable String tenantId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(applicationService.findByTenantId(tenantId, pageable));
    }

    // GET /api/tenants/{tenantId}/applications/{id}
    @GetMapping("/tenants/{tenantId}/applications/{id}")
    public ResponseEntity<ApplicationResponse> findById(
            @PathVariable String tenantId,
            @PathVariable String id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    // PUT /api/tenants/{tenantId}/applications/{id}
    @PutMapping("/tenants/{tenantId}/applications/{id}")
    public ResponseEntity<ApplicationResponse> update(
            @PathVariable String tenantId,
            @PathVariable String id,
            @Valid @RequestBody UpdateApplicationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String ownerId = extractUsername(jwt);
        return ResponseEntity.ok(applicationService.update(id, request, ownerId));
    }

    // PATCH /api/tenants/{tenantId}/applications/{id}/status
    @PatchMapping("/tenants/{tenantId}/applications/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable String tenantId,
            @PathVariable String id,
            @RequestParam ApplicationStatus status,
            @AuthenticationPrincipal Jwt jwt) {
        String ownerId = extractUsername(jwt);
        applicationService.changeStatus(id, status, ownerId);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/tenants/{tenantId}/applications/{id}
    @DeleteMapping("/tenants/{tenantId}/applications/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String tenantId,
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        String ownerId = extractUsername(jwt);
        applicationService.delete(id, ownerId);
        return ResponseEntity.noContent().build();
    }

    // GET /api/applications  (admin: all applications across all tenants)
    @GetMapping("/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ApplicationResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(applicationService.findAll(pageable));
    }

    private String extractUsername(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "JWT is required");
        }
        String username = jwt.getClaimAsString("preferred_username");
        if (username == null || username.isBlank()) {
            username = jwt.getSubject();
        }
        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unable to resolve username from JWT");
        }
        return username;
    }
}


package com.avira.projectservice.tenant.controller;

import com.avira.projectservice.tenant.dto.CreateTenantRequest;
import com.avira.projectservice.tenant.dto.TenantResponse;
import com.avira.projectservice.tenant.dto.UpdateTenantRequest;
import com.avira.projectservice.tenant.enums.TenantStatus;
import com.avira.projectservice.tenant.service.TenantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    // GET /api/tenants?page=0&size=20
    @GetMapping
    public ResponseEntity<Page<TenantResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(tenantService.findAll(pageable));
    }

    // GET /api/tenants/{id}
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(tenantService.findById(id));
    }

    // GET /api/tenants/owner/{ownerId}
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Page<TenantResponse>> findByOwnerId(
            @PathVariable String ownerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(tenantService.findByOwnerId(ownerId, pageable));
    }

    // POST /api/tenants
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TenantResponse> create(
            @Valid @RequestBody CreateTenantRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String ownerId = extractUsername(jwt);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tenantService.create(request, ownerId));
    }

    // PUT /api/tenants/{id}
    @PutMapping("/{id}")
    public ResponseEntity<TenantResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.update(id, request));
    }

    // PATCH /api/tenants/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable String id,
            @RequestParam TenantStatus status) {
        tenantService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/tenants/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        tenantService.delete(id);
        return ResponseEntity.noContent().build();
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

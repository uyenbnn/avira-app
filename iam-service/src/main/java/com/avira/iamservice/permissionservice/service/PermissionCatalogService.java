package com.avira.iamservice.permissionservice.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.avira.iamservice.permissionservice.dto.RolePermissionResponse;
import com.avira.iamservice.roleservice.util.RoleConstants;

@Service
public class PermissionCatalogService {

    private static final Map<String, Set<String>> ROLE_PERMISSIONS = Map.of(
            RoleConstants.ADMIN, Set.of("tenant:read", "tenant:write", "user:read", "user:write"),
            RoleConstants.USER, Set.of("tenant:read", "user:read")
    );

    public RolePermissionResponse findByRole(String role) {
        Set<String> permissions = ROLE_PERMISSIONS.getOrDefault(role, Set.of());
        return new RolePermissionResponse(role, permissions);
    }
}

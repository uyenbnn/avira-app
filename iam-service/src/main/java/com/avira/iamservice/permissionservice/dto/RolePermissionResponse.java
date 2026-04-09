package com.avira.iamservice.permissionservice.dto;

import java.util.Set;

public record RolePermissionResponse(String role, Set<String> permissions) {
}

package com.avira.iamservice.roleservice.dto;

import java.util.Set;
import java.util.UUID;

public record RoleAssignmentResponse(UUID userId, Set<String> roles) {
}

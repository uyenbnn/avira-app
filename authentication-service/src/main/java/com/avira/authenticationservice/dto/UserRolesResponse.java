package com.avira.authenticationservice.dto;

import java.util.Set;

public record UserRolesResponse(
        String userId,
        Set<String> roles
) {
}


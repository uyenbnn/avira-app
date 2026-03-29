package com.avira.authenticationservice.dto;

import java.util.Set;

public record UpdateUserRolesRequest(
        Set<String> roles
) {
}


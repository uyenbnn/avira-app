package com.avira.projectservice.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record UpdateTenantRequest(
        String name,

        String description,

        @Positive(message = "Max users must be greater than 0")
        Integer maxUsers
) {
}


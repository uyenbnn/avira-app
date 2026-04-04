package com.avira.projectservice.application.dto;

import com.avira.projectservice.application.enums.ApplicationKind;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateApplicationRequest(

        @Size(max = 255, message = "Application name must not exceed 255 characters")
        String name,

        ApplicationKind kind,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Pattern(
                regexp = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}$|^$",
                message = "Domain must be a valid hostname (e.g. myapp.example.com) or empty"
        )
        String domain
) {
}


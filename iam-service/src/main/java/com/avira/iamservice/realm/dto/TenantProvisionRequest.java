package com.avira.iamservice.realm.dto;

import com.avira.iamservice.realm.IdentityMode;

public record TenantProvisionRequest(
        String tenantId,
        String tenantName,
        String contactEmail,
        IdentityMode identityMode
) {
}

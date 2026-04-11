package com.avira.iamservice.realm.dto;

import com.avira.iamservice.realm.IdentityMode;

public record RealmResolutionResponse(String tenantId, String realm, IdentityMode identityMode) {
}

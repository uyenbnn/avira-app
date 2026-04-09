package com.avira.iamservice.clientservice.controller;

import com.avira.iamservice.clientservice.service.RealmResolver;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/realms")
public class TenantRealmController {

    private final RealmResolver realmResolver;

    public TenantRealmController(RealmResolver realmResolver) {
        this.realmResolver = realmResolver;
    }

    @GetMapping("/tenants/{tenantId}")
    public Map<String, String> resolveTenantRealm(@PathVariable String tenantId) {
        return Map.of("tenantId", tenantId, "realm", realmResolver.resolveRealm(tenantId));
    }
}



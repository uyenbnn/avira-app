package com.avira.iamservice.clientservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.clientservice.dto.ResolvedRealmResponse;
import com.avira.iamservice.clientservice.service.RealmResolver;

@RestController
@RequestMapping("/api/iam/client/realms")
public class RealmResolverController {

    private final RealmResolver realmResolver;

    public RealmResolverController(RealmResolver realmResolver) {
        this.realmResolver = realmResolver;
    }

    @GetMapping("/tenants/{tenantId}")
    public ResolvedRealmResponse resolve(@PathVariable String tenantId) {
        return new ResolvedRealmResponse(tenantId, realmResolver.resolveRealm(tenantId));
    }
}

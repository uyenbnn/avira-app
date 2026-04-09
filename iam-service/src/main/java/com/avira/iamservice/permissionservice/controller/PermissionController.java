package com.avira.iamservice.permissionservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.permissionservice.dto.RolePermissionResponse;
import com.avira.iamservice.permissionservice.service.PermissionCatalogService;

@RestController
@RequestMapping("/api/iam/permissions")
public class PermissionController {

    private final PermissionCatalogService permissionCatalogService;

    public PermissionController(PermissionCatalogService permissionCatalogService) {
        this.permissionCatalogService = permissionCatalogService;
    }

    @GetMapping("/roles/{role}")
    public RolePermissionResponse findByRole(@PathVariable String role) {
        return permissionCatalogService.findByRole(role);
    }
}

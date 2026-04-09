package com.avira.iamservice.roleservice.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.roleservice.dto.AssignRoleRequest;
import com.avira.iamservice.roleservice.dto.RoleAssignmentResponse;
import com.avira.iamservice.roleservice.service.RoleService;

@RestController
@RequestMapping("/api/iam/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/users/{userId}")
    public RoleAssignmentResponse assign(@PathVariable UUID userId, @RequestBody AssignRoleRequest request) {
        return roleService.assign(userId, request.getRole());
    }

    @GetMapping("/users/{userId}")
    public RoleAssignmentResponse findByUserId(@PathVariable UUID userId) {
        return roleService.findByUserId(userId);
    }
}

package com.avira.iamservice.roleservice.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.avira.iamservice.roleservice.dto.RoleAssignmentResponse;
import com.avira.iamservice.roleservice.util.RoleConstants;

@Service
public class RoleService {

    private final Map<UUID, Set<String>> assignments = new ConcurrentHashMap<>();

    public RoleAssignmentResponse assign(UUID userId, String role) {
        if (!RoleConstants.SUPPORTED.contains(role)) {
            throw new IllegalArgumentException("unsupported role: " + role);
        }

        assignments.computeIfAbsent(userId, ignored -> new HashSet<>()).add(role);
        return new RoleAssignmentResponse(userId, Set.copyOf(assignments.get(userId)));
    }

    public RoleAssignmentResponse findByUserId(UUID userId) {
        return new RoleAssignmentResponse(userId, Set.copyOf(assignments.getOrDefault(userId, Set.of())));
    }
}

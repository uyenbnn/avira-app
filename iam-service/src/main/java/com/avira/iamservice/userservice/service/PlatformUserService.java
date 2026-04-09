package com.avira.iamservice.userservice.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.avira.iamservice.userservice.domain.PlatformUser;
import com.avira.iamservice.userservice.dto.CreatePlatformUserRequest;
import com.avira.iamservice.userservice.dto.PlatformUserResponse;
import com.avira.iamservice.userservice.repository.PlatformUserRepository;

@Service
public class PlatformUserService {

    private static final String ACTIVE = "ACTIVE";

    private final PlatformUserRepository platformUserRepository;

    public PlatformUserService(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    @Transactional
    public PlatformUserResponse create(CreatePlatformUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (platformUserRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("username already exists");
        }
        if (platformUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("email already exists");
        }

        PlatformUser created = platformUserRepository.save(new PlatformUser(
                UUID.randomUUID(),
                request.getUsername(),
                request.getEmail(),
                ACTIVE
        ));

        return toResponse(created);
    }

    @Transactional(readOnly = true)
    public List<PlatformUserResponse> findAll() {
        return platformUserRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PlatformUserResponse findById(UUID userId) {
        PlatformUser user = platformUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + userId));
        return toResponse(user);
    }

    private PlatformUserResponse toResponse(PlatformUser user) {
        return new PlatformUserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }
}

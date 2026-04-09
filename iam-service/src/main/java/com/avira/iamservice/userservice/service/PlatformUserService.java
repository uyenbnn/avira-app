package com.avira.iamservice.userservice.service;

import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.NotFoundException;
import com.avira.iamservice.userservice.domain.PlatformUser;
import com.avira.iamservice.userservice.dto.CreatePlatformUserRequest;
import com.avira.iamservice.userservice.dto.PlatformUserResponse;
import com.avira.iamservice.userservice.repository.PlatformUserRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformUserService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final PlatformUserRepository platformUserRepository;

    public PlatformUserService(PlatformUserRepository platformUserRepository) {
        this.platformUserRepository = platformUserRepository;
    }

    @Transactional
    public PlatformUserResponse create(CreatePlatformUserRequest request) {
        if (platformUserRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }
        if (platformUserRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        PlatformUser user = new PlatformUser(
                UUID.randomUUID(),
                request.getUsername(),
                request.getEmail(),
                ACTIVE_STATUS,
                OffsetDateTime.now()
        );

        return toResponse(platformUserRepository.save(user));
    }

    @Transactional(readOnly = true)
    public PlatformUserResponse getById(UUID id) {
        PlatformUser user = platformUserRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Platform user not found: " + id));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<PlatformUserResponse> getAll() {
        return platformUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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


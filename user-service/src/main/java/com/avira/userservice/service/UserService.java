package com.avira.userservice.service;

import com.avira.userservice.client.KeycloakAdminClient;
import com.avira.userservice.constants.RoleConstants;
import com.avira.userservice.dto.CreateUserRequest;
import com.avira.userservice.dto.UpdateUserRequest;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.entity.User;
import com.avira.userservice.entity.UserAuthProvider;
import com.avira.userservice.entity.UserProfile;
import com.avira.userservice.enums.AuthProvider;
import com.avira.userservice.enums.UserStatus;
import com.avira.userservice.repository.UserAuthProviderRepository;
import com.avira.userservice.repository.UserProfileRepository;
import com.avira.userservice.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserAuthProviderRepository userAuthProviderRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    // ------------------------------------------------------------------ //
    //  Queries
    // ------------------------------------------------------------------ //

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse findById(String id) {
        return toResponse(getOrThrow(id));
    }

    // ------------------------------------------------------------------ //
    //  Commands
    // ------------------------------------------------------------------ //

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String keycloakId = keycloakAdminClient.createUser(
                        request.email(),
                        request.firstName(),
                        request.lastName(),
                        request.password(),
                        false)
                .orElseThrow(() -> new IllegalStateException(
                        "Failed to create Keycloak identity for email: " + request.email()));

        try {
            User user = userRepository.save(User.builder()
                    .phone(request.phone())
                    .status(UserStatus.ACTIVE)
                    .build());

            userProfileRepository.save(UserProfile.builder()
                    .user(user)
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .build());

            UserAuthProvider authProvider = userAuthProviderRepository.save(UserAuthProvider.builder()
                    .user(user)
                    .provider(AuthProvider.LOCAL)
                    .providerUserId(keycloakId)
                    .email(request.email())
                    .build());

            keycloakAdminClient.assignRole(keycloakId, RoleConstants.USER);

            log.info("Created domain user id={} linkedToProvider={} providerUserId={}",
                    user.getId(), authProvider.getProvider(), authProvider.getProviderUserId());
            return toResponse(user, authProvider);
        } catch (RuntimeException e) {
            try {
                keycloakAdminClient.deleteUser(keycloakId);
            } catch (Exception cleanupError) {
                log.error("Failed to rollback Keycloak identity {} after local create error: {}",
                        keycloakId, cleanupError.getMessage());
            }
            throw e;
        }
    }

    @Transactional
    public UserResponse update(String id, UpdateUserRequest request) {
        User user = getOrThrow(id);

        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        user = userRepository.save(user);
        log.info("Updated domain user id={}", id);
        return toResponse(user);
    }

    @Transactional
    public void changeStatus(String id, UserStatus status) {
        User user = getOrThrow(id);
        user.setStatus(status);
        userRepository.save(user);

        boolean enabled = status == UserStatus.ACTIVE;
        findLocalAuthProvider(id)
                .ifPresentOrElse(
                        authProvider -> keycloakAdminClient.setUserEnabled(authProvider.getProviderUserId(), enabled),
                        () -> log.warn("No linked identity provider found for domain user id={} while changing status", id)
                );

        log.info("Changed status of domain user id={} to {}", id, status);
    }

    @Transactional
    public void delete(String id) {
        User user = getOrThrow(id);

        findLocalAuthProvider(id)
                .ifPresent(authProvider -> keycloakAdminClient.deleteUser(authProvider.getProviderUserId()));

        userAuthProviderRepository.deleteByUserId(id);
        userProfileRepository.deleteById(id);
        userRepository.delete(user);
        log.info("Deleted domain user id={}", id);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private User getOrThrow(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User user) {
        return toResponse(user, findLocalAuthProvider(user.getId()).orElse(null));
    }

    private UserResponse toResponse(User user, UserAuthProvider authProvider) {
        UserRepresentation identity = null;
        if (authProvider != null && authProvider.getProviderUserId() != null && !authProvider.getProviderUserId().isBlank()) {
            identity = keycloakAdminClient.findById(authProvider.getProviderUserId()).orElse(null);
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(identity != null ? identity.getEmail() : authProvider != null ? authProvider.getEmail() : null)
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(identity != null && Boolean.TRUE.equals(identity.isEmailVerified()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private java.util.Optional<UserAuthProvider> findLocalAuthProvider(String userId) {
        return userAuthProviderRepository.findFirstByUserIdAndProvider(userId, AuthProvider.LOCAL);
    }
}

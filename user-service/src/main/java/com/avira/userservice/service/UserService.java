package com.avira.userservice.service;

import com.avira.userservice.client.KeycloakAdminClient;
import com.avira.userservice.constants.RoleConstants;
import com.avira.userservice.dto.CreateUserRequest;
import com.avira.userservice.dto.UpdateUserRequest;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.entity.Role;
import com.avira.userservice.entity.User;
import com.avira.userservice.entity.UserProfile;
import com.avira.userservice.entity.UserRole;
import com.avira.userservice.enums.UserStatus;
import com.avira.userservice.repository.RoleRepository;
import com.avira.userservice.repository.UserProfileRepository;
import com.avira.userservice.repository.UserRepository;
import com.avira.userservice.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final KeycloakAdminClient keycloakAdminClient;
    private final PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------------ //
    //  Queries
    // ------------------------------------------------------------------ //

    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    public UserResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    public UserResponse findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "User not found with email: " + email));
    }

    // ------------------------------------------------------------------ //
    //  Commands
    // ------------------------------------------------------------------ //

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .status(UserStatus.ACTIVE)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Create an empty profile linked to the user
        UserProfile profile = UserProfile.builder().user(user).build();
        userProfileRepository.save(profile);

        // Ensure base roles exist and assign USER as default role for new registrations.
        ensureBaseRoles();
        Role userRole = roleRepository.findById(RoleConstants.USER)
                .orElseThrow(() -> new IllegalStateException("Role USER is missing after initialization"));
        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(userRole)
                .build());

        // Mirror to Keycloak (best-effort — don't fail the transaction)
        try {
            var keycloakUserId = keycloakAdminClient.createUser(
                    user.getEmail(), request.firstName(), request.lastName(),
                    request.password(), false);
            keycloakUserId.ifPresent(id -> keycloakAdminClient.assignRole(id, RoleConstants.USER));
        } catch (Exception e) {
            log.warn("Keycloak sync failed for new user {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("Created user id={} email={}", user.getId(), user.getEmail());
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = getOrThrow(id);

        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new IllegalArgumentException("Email already taken: " + request.email());
            }
            user.setEmail(request.email());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }

        user = userRepository.save(user);
        log.info("Updated user id={}", id);
        return toResponse(user);
    }

    @Transactional
    public void changeStatus(UUID id, UserStatus status) {
        User user = getOrThrow(id);
        user.setStatus(status);
        userRepository.save(user);

        // Mirror status to Keycloak: ACTIVE -> enabled, otherwise disabled.
        try {
            boolean enabled = status == UserStatus.ACTIVE;
            keycloakAdminClient.setUserEnabledByEmail(user.getEmail(), enabled);
        } catch (Exception e) {
            log.warn("Keycloak status sync failed for user {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("Changed status of user id={} to {}", id, status);
    }

    @Transactional
    public void delete(UUID id) {
        User user = getOrThrow(id);
        userRepository.delete(user);
        log.info("Deleted user id={}", id);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private User getOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "User not found: " + id));
    }

    private void ensureBaseRoles() {
        for (String roleName : RoleConstants.BASE_ROLES) {
            ensureRoleExists(roleName);
        }
    }

    private void ensureRoleExists(String roleName) {
        if (roleRepository.existsById(roleName)) {
            return;
        }
        roleRepository.save(Role.builder().name(roleName).build());
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .id(u.getId())
                .email(u.getEmail())
                .phone(u.getPhone())
                .status(u.getStatus())
                .emailVerified(u.isEmailVerified())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .lastLoginAt(u.getLastLoginAt())
                .build();
    }
}

package com.avira.userservice.service;

import com.avira.commonlib.messaging.user.UserRegisteredEvent;
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
        User user = createLocalUserRecord(null, request.email(), request.phone(), request.firstName(), request.lastName());

        UserAuthProvider authProvider = findLocalAuthProvider(user.getId()).orElse(null);
        log.info("Created domain-only user id={} linkedToProvider={} providerUserId={}",
                user.getId(), authProvider != null ? authProvider.getProvider() : null,
                authProvider != null ? authProvider.getProviderUserId() : null);
        return toResponse(user, authProvider);
    }

    @Transactional
    public void createFromRegisteredEvent(UserRegisteredEvent event) {
        if (event == null || event.userId() == null || event.userId().isBlank()) {
            throw new IllegalArgumentException("Registered event userId is required");
        }

        if (userRepository.existsById(event.userId())) {
            log.info("Skipping user-domain registered event because user '{}' already exists", event.userId());
            return;
        }

        createLocalUserRecord(event.userId(), event.email(), null, event.firstName(), event.lastName());
        log.info("Created local user '{}' from registration event", event.userId());
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

        log.info("Changed status of domain user id={} to {}", id, status);
    }

    @Transactional
    public void delete(String id) {
        User user = getOrThrow(id);

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

    private User createLocalUserRecord(String userId,
                                       String email,
                                       String phone,
                                       String firstName,
                                       String lastName) {
        User user = userRepository.save(User.builder()
                .id(userId)
                .phone(phone)
                .status(UserStatus.ACTIVE)
                .build());

        String providerUserId = (userId != null && !userId.isBlank()) ? userId : user.getId();

        userProfileRepository.save(UserProfile.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .build());

        userAuthProviderRepository.save(UserAuthProvider.builder()
                .user(user)
                .provider(AuthProvider.LOCAL)
                .providerUserId(providerUserId)
                .email(email)
                .build());
        return user;
    }

    private UserResponse toResponse(User user) {
        return toResponse(user, findLocalAuthProvider(user.getId()).orElse(null));
    }

    private UserResponse toResponse(User user, UserAuthProvider authProvider) {
        return UserResponse.builder()
                .id(user.getId())
                .email(authProvider != null ? authProvider.getEmail() : null)
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(false)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    private java.util.Optional<UserAuthProvider> findLocalAuthProvider(String userId) {
        return userAuthProviderRepository.findFirstByUserIdAndProvider(userId, AuthProvider.LOCAL);
    }
}

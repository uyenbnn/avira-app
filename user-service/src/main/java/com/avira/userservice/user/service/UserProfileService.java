package com.avira.userservice.user.service;

import com.avira.userservice.user.dto.UpdateUserProfileRequest;
import com.avira.userservice.user.dto.UserProfileResponse;
import com.avira.userservice.user.entity.UserProfile;
import com.avira.userservice.user.repository.UserProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResponse findByUserId(String userId) {
        UserProfile profile = getOrThrow(userId);
        return toResponse(profile);
    }

    @Transactional
    public UserProfileResponse update(String userId, UpdateUserProfileRequest request) {
        UserProfile profile = getOrThrow(userId);

        if (request.firstName() != null) profile.setFirstName(request.firstName());
        if (request.lastName()  != null) profile.setLastName(request.lastName());
        if (request.avatarUrl() != null) profile.setAvatarUrl(request.avatarUrl());
        if (request.birthDate() != null) profile.setBirthDate(request.birthDate());
        if (request.gender()    != null) profile.setGender(request.gender());

        profile = userProfileRepository.save(profile);
        log.info("Updated profile for userId={}", userId);
        return toResponse(profile);
    }

    // ------------------------------------------------------------------ //
    //  Helpers
    // ------------------------------------------------------------------ //

    private UserProfile getOrThrow(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "User profile not found for userId: " + userId));
    }

    private UserProfileResponse toResponse(UserProfile p) {
        return UserProfileResponse.builder()
                .userId(p.getUserId())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .avatarUrl(p.getAvatarUrl())
                .birthDate(p.getBirthDate())
                .gender(p.getGender())
                .build();
    }
}

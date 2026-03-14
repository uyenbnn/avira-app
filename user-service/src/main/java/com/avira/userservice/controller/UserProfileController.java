package com.avira.userservice.controller;

import com.avira.userservice.dto.UpdateUserProfileRequest;
import com.avira.userservice.dto.UserProfileResponse;
import com.avira.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    // GET /api/v1/users/{userId}/profile
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<UserProfileResponse> findByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(userProfileService.findByUserId(userId));
    }

    // PUT /api/v1/users/{userId}/profile
    @PutMapping
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #userId.toString()")
    public ResponseEntity<UserProfileResponse> update(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return ResponseEntity.ok(userProfileService.update(userId, request));
    }
}


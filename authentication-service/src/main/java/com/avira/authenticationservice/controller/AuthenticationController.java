package com.avira.authenticationservice.controller;

import com.avira.authenticationservice.dto.LoginRequest;
import com.avira.authenticationservice.dto.LogoutRequest;
import com.avira.authenticationservice.dto.RefreshTokenRequest;
import com.avira.authenticationservice.dto.RegisterRequest;
import com.avira.authenticationservice.dto.TokenResponse;
import com.avira.authenticationservice.dto.UpdateUserRolesRequest;
import com.avira.authenticationservice.dto.UserResponse;
import com.avira.authenticationservice.dto.UserRolesResponse;
import com.avira.authenticationservice.service.AuthenticationOrchestrationService;
import com.avira.commonlib.constants.AuthApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationOrchestrationService authenticationOrchestrationService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationOrchestrationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationOrchestrationService.login(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationOrchestrationService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
        authenticationOrchestrationService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{userId}/roles")
    public ResponseEntity<UserRolesResponse> updateRoles(
            @PathVariable String userId,
            @RequestBody UpdateUserRolesRequest request) {
        return ResponseEntity.ok(authenticationOrchestrationService.updateRoles(userId, request));
    }
}

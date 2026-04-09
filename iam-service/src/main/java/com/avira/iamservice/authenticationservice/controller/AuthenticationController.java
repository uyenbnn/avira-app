package com.avira.iamservice.authenticationservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.avira.iamservice.authenticationservice.dto.LoginRequest;
import com.avira.iamservice.authenticationservice.dto.LogoutRequest;
import com.avira.iamservice.authenticationservice.dto.RefreshTokenRequest;
import com.avira.iamservice.authenticationservice.dto.TokenResponse;
import com.avira.iamservice.authenticationservice.service.AuthenticationService;

@RestController
@RequestMapping("/api/iam/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        return authenticationService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@RequestBody RefreshTokenRequest request) {
        return authenticationService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestBody LogoutRequest request) {
        authenticationService.logout(request.getRefreshToken());
    }
}

package com.avira.userservice.service;

import com.avira.userservice.client.KeycloakAuthClient;
import com.avira.userservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final KeycloakAuthClient keycloakAuthClient;

    public UserResponse register(RegisterRequest request) {
        CreateUserRequest createUser = new CreateUserRequest(
                request.email(),
                request.password(),
                request.phone(),
                request.firstName(),
                request.lastName()
        );
        return userService.create(createUser);
    }

    public TokenResponse login(LoginRequest request) {
        return keycloakAuthClient.login(request.email(), request.password());
    }
}


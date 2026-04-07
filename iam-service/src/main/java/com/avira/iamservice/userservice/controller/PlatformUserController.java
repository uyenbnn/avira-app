package com.avira.iamservice.userservice.controller;

import com.avira.iamservice.userservice.dto.CreatePlatformUserRequest;
import com.avira.iamservice.userservice.dto.PlatformUserResponse;
import com.avira.iamservice.userservice.service.PlatformUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iam/users")
public class PlatformUserController {

    private final PlatformUserService platformUserService;

    public PlatformUserController(PlatformUserService platformUserService) {
        this.platformUserService = platformUserService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlatformUserResponse create(@Valid @RequestBody CreatePlatformUserRequest request) {
        return platformUserService.create(request);
    }

    @GetMapping("/{id}")
    public PlatformUserResponse getById(@PathVariable UUID id) {
        return platformUserService.getById(id);
    }

    @GetMapping
    public List<PlatformUserResponse> getAll() {
        return platformUserService.getAll();
    }
}


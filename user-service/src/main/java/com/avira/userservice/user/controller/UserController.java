package com.avira.userservice.user.controller;

import com.avira.userservice.user.dto.CreateUserRequest;
import com.avira.userservice.user.dto.UpdateUserRequest;
import com.avira.userservice.user.dto.UserResponse;
import com.avira.userservice.user.enums.UserStatus;
import com.avira.userservice.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // GET /api/v1/users?page=0&size=20
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.findAll(pageable));
    }

    // GET /api/v1/users/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAuthorization.canAccessUser(authentication, #id)")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    // POST /api/v1/users
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.create(request));
    }

    // PUT /api/v1/users/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAuthorization.canAccessUser(authentication, #id)")
    public ResponseEntity<UserResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    // PATCH /api/v1/users/{id}/status
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable String id,
            @RequestParam UserStatus status) {
        userService.changeStatus(id, status);
        return ResponseEntity.noContent().build();
    }

    // DELETE /api/v1/users/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

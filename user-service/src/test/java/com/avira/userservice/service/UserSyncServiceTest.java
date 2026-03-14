package com.avira.userservice.service;

import com.avira.userservice.client.KeycloakAdminClient;
import com.avira.userservice.constants.RoleConstants;
import com.avira.userservice.dto.CreateUserRequest;
import com.avira.userservice.dto.UserResponse;
import com.avira.userservice.entity.Role;
import com.avira.userservice.entity.User;
import com.avira.userservice.entity.UserProfile;
import com.avira.userservice.entity.UserRole;
import com.avira.userservice.repository.RoleRepository;
import com.avira.userservice.repository.UserProfileRepository;
import com.avira.userservice.repository.UserRepository;
import com.avira.userservice.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private KeycloakAdminClient keycloakAdminClient;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserAndProfileAndSyncToKeycloak() {
        CreateUserRequest request = new CreateUserRequest(
                "alice@avira.com",
                "StrongPass123",
                "0123456789",
                "Alice",
                "Smith"
        );

        when(userRepository.existsByEmail("alice@avira.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.existsById(any(String.class))).thenReturn(false);
        when(roleRepository.findById("USER")).thenReturn(Optional.of(Role.builder().name("USER").build()));
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(keycloakAdminClient.createUser(eq("alice@avira.com"), eq(null), eq(null), eq("StrongPass123"), eq(false)))
                .thenReturn(Optional.of("kc-user-id"));

        UserResponse result = userService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("alice@avira.com");
        assertThat(result.phone()).isEqualTo("0123456789");
        assertThat(result.id()).isNotNull();

        verify(userRepository).existsByEmail("alice@avira.com");
        verify(passwordEncoder).encode("StrongPass123");
        verify(userRepository).save(any(User.class));
        verify(userProfileRepository).save(any(UserProfile.class));
        verify(roleRepository).existsById("USER");
        verify(roleRepository).existsById("ADMIN");
        verify(roleRepository).existsById("SELLER");
        verify(roleRepository).existsById("BUYER");
        verify(roleRepository).findById("USER");
        verify(userRoleRepository).save(any(UserRole.class));
        verify(keycloakAdminClient).createUser("alice@avira.com", null, null, "StrongPass123", false);
        verify(keycloakAdminClient).assignRole("kc-user-id", RoleConstants.USER);
    }
}

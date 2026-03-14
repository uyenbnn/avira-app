package com.avira.userservice.repository;

import com.avira.userservice.entity.UserAuthProvider;
import com.avira.userservice.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, UUID> {

    List<UserAuthProvider> findAllByUserId(UUID userId);

    Optional<UserAuthProvider> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<UserAuthProvider> findByProviderAndEmail(AuthProvider provider, String email);
}


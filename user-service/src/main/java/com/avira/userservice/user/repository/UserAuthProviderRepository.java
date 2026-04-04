package com.avira.userservice.user.repository;

import com.avira.userservice.user.entity.UserAuthProvider;
import com.avira.userservice.user.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, String> {

    List<UserAuthProvider> findAllByUserId(String userId);

    Optional<UserAuthProvider> findFirstByUserIdAndProvider(String userId, AuthProvider provider);

    Optional<UserAuthProvider> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<UserAuthProvider> findByProviderAndEmail(AuthProvider provider, String email);

    long deleteByUserId(String userId);
}

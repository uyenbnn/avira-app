package com.avira.iamservice.userservice.repository;

import com.avira.iamservice.userservice.domain.PlatformUser;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<PlatformUser> findByUsername(String username);
}


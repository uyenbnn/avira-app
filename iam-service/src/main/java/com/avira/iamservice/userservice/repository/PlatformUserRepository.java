package com.avira.iamservice.userservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avira.iamservice.userservice.domain.PlatformUser;

public interface PlatformUserRepository extends JpaRepository<PlatformUser, UUID> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}

package com.avira.userservice.repository;

import com.avira.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    List<UserRole> findAllByUserId(UUID userId);

    boolean existsByUserIdAndRoleName(UUID userId, String roleName);

    @org.springframework.transaction.annotation.Transactional
    void deleteByUserIdAndRoleName(UUID userId, String roleName);
}




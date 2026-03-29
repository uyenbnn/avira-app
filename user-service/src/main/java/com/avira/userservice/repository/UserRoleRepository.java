package com.avira.userservice.repository;

import com.avira.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, String> {

    List<UserRole> findAllByUserId(String userId);

    boolean existsByUserIdAndRoleName(String userId, String roleName);

    @org.springframework.transaction.annotation.Transactional
    void deleteByUserIdAndRoleName(String userId, String roleName);
}

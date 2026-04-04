package com.avira.userservice.user.repository;

import com.avira.userservice.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

    Optional<UserProfile> findByUserId(String userId);
}

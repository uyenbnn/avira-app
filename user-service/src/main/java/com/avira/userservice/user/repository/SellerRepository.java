package com.avira.userservice.user.repository;

import com.avira.userservice.user.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, String> {

    Optional<Seller> findByUserId(String userId);

    boolean existsByUserId(String userId);
}

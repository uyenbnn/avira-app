package com.avira.projectservice.tenant.repository;

import com.avira.projectservice.tenant.entity.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByName(String name);

    Page<Tenant> findByOwnerId(String ownerId, Pageable pageable);
}


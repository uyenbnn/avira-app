package com.avira.iamservice.clientservice.repository;

import com.avira.iamservice.clientservice.domain.TenantRealmConfig;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRealmConfigRepository extends JpaRepository<TenantRealmConfig, UUID> {
}


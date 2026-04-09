package com.avira.iamservice.clientservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.avira.iamservice.clientservice.domain.TenantRealmConfig;

public interface TenantRealmConfigRepository extends JpaRepository<TenantRealmConfig, UUID> {
}

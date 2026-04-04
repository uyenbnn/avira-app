package com.avira.projectservice.application.repository;

import com.avira.projectservice.application.entity.Application;
import com.avira.projectservice.application.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, String> {

    Page<Application> findByTenantId(String tenantId, Pageable pageable);

    Page<Application> findByTenantIdAndStatus(String tenantId, ApplicationStatus status, Pageable pageable);

    Optional<Application> findByTenantIdAndName(String tenantId, String name);

    boolean existsByDomain(String domain);
}


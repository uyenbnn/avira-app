package com.avira.projectservice.tenant.service;

import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.TenantDomainActions;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.NotFoundException;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.projectservice.tenant.dto.CreateTenantRequest;
import com.avira.projectservice.tenant.dto.TenantResponse;
import com.avira.projectservice.tenant.dto.UpdateTenantRequest;
import com.avira.projectservice.tenant.entity.Tenant;
import com.avira.projectservice.tenant.enums.TenantStatus;
import com.avira.projectservice.tenant.event.TenantCreatedEvent;
import com.avira.projectservice.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public TenantResponse create(CreateTenantRequest request, String ownerId) {
        // Check if tenant with the same name already exists
        if (tenantRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Tenant with name '" + request.name() + "' already exists");
        }

        Tenant tenant = Tenant.builder()
                .name(request.name())
                .description(request.description())
                .ownerId(ownerId)
                .status(TenantStatus.ACTIVE)
                .maxUsers(request.maxUsers() != null ? request.maxUsers() : 100)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        log.info("Created tenant id={} name={} ownerId={}", saved.getId(), saved.getName(), saved.getOwnerId());

        // Publish TenantCreatedEvent
        publishTenantCreatedEvent(saved);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TenantResponse findById(String id) {
        return tenantRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> findAll(Pageable pageable) {
        return tenantRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TenantResponse> findByOwnerId(String ownerId, Pageable pageable) {
        return tenantRepository.findByOwnerId(ownerId, pageable).map(this::toResponse);
    }

    @Transactional
    public TenantResponse update(String id, UpdateTenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            tenant.setName(request.name());
        }
        if (request.description() != null) {
            tenant.setDescription(request.description());
        }
        if (request.maxUsers() != null && request.maxUsers() > 0) {
            tenant.setMaxUsers(request.maxUsers());
        }

        Tenant updated = tenantRepository.save(tenant);
        log.info("Updated tenant id={} name={}", updated.getId(), updated.getName());

        return toResponse(updated);
    }

    @Transactional
    public void changeStatus(String id, TenantStatus status) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));

        tenant.setStatus(status);
        tenantRepository.save(tenant);
        log.info("Changed tenant id={} status={}", id, status);
    }

    @Transactional
    public void delete(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));

        tenant.setStatus(TenantStatus.DELETED);
        tenantRepository.save(tenant);
        log.info("Deleted tenant id={} name={}", id, tenant.getName());
    }

    private void publishTenantCreatedEvent(Tenant tenant) {
        TenantCreatedEvent payload = new TenantCreatedEvent(
                tenant.getId(),
                tenant.getName(),
                tenant.getDescription(),
                tenant.getOwnerId(),
                tenant.getMaxUsers()
        );

        eventPublisher.publish(
                EventTopics.TENANT_DOMAIN,
                TenantDomainActions.CREATED,
                "project-service",
                tenant.getId(),
                tenant.getId(),
                payload,
                java.util.Map.of()
        );
        log.info("Published TenantCreatedEvent for tenant id={}", tenant.getId());
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getDescription(),
                tenant.getOwnerId(),
                tenant.getStatus(),
                tenant.getMaxUsers(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}

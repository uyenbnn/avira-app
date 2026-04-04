package com.avira.projectservice.tenant.service;

import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.constants.TenantDomainActions;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.NotFoundException;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.tenant.TenantAuthenticationEnabledEvent;
import com.avira.commonlib.messaging.tenant.TenantCreatedEvent;
import com.avira.commonlib.messaging.user.UserRegisteredEvent;
import com.avira.projectservice.tenant.dto.CreateTenantRequest;
import com.avira.projectservice.tenant.dto.TenantResponse;
import com.avira.projectservice.tenant.dto.UpdateTenantRequest;
import com.avira.projectservice.tenant.entity.Tenant;
import com.avira.projectservice.tenant.enums.TenantStatus;
import com.avira.projectservice.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
        // Enforce 1-tenant-per-user
        Page<Tenant> existing = tenantRepository.findByOwnerId(ownerId, PageRequest.of(0, 1));
        if (!existing.isEmpty()) {
            throw new ConflictException("User '" + ownerId + "' already has a tenant");
        }

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
                .authenticationEnabled(Boolean.TRUE.equals(request.authenticationEnabled()))
                .build();

        Tenant saved = tenantRepository.save(tenant);
        log.info("Created tenant id={} name={} ownerId={} authenticationEnabled={}",
                saved.getId(), saved.getName(), saved.getOwnerId(), saved.getAuthenticationEnabled());

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
        if (request.authenticationEnabled() != null) {
            tenant.setAuthenticationEnabled(request.authenticationEnabled());
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
    public TenantResponse enableAuthentication(String id, boolean enabled) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));

        boolean wasEnabled = Boolean.TRUE.equals(tenant.getAuthenticationEnabled());
        tenant.setAuthenticationEnabled(enabled);
        Tenant saved = tenantRepository.save(tenant);
        log.info("Set authenticationEnabled={} for tenant id={}", enabled, id);

        if (enabled && !wasEnabled) {
            publishTenantAuthenticationEnabledEvent(saved);
        }

        return toResponse(saved);
    }

    @Transactional
    public void delete(String id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + id));

        tenant.setStatus(TenantStatus.DELETED);
        tenantRepository.save(tenant);
        log.info("Deleted tenant id={} name={}", id, tenant.getName());
    }

    /**
     * Auto-creates a default tenant for a newly registered user.
     * Called by {@link com.avira.projectservice.messaging.UserRegisteredEventConsumer} on user-domain.registered events.
     * Idempotent: skips creation if a tenant already exists for the owner.
     */
    @Transactional
    public void createDefaultTenantForUser(UserRegisteredEvent event) {
        Page<Tenant> existing = tenantRepository.findByOwnerId(event.username(), PageRequest.of(0, 1));
        if (!existing.isEmpty()) {
            log.info("Tenant already exists for user '{}', skipping auto-creation", event.username());
            return;
        }

        String tenantName = event.username() + "-workspace";
        if (tenantRepository.findByName(tenantName).isPresent()) {
            tenantName = event.username() + "-" + event.userId().substring(0, 8) + "-workspace";
        }

        Tenant tenant = Tenant.builder()
                .name(tenantName)
                .description("Default workspace for " + event.username())
                .ownerId(event.username())
                .status(TenantStatus.ACTIVE)
                .maxUsers(100)
                .authenticationEnabled(false)
                .build();

        Tenant saved = tenantRepository.save(tenant);
        log.info("Auto-created default tenant id={} name={} for user={}",
                saved.getId(), saved.getName(), event.username());

        publishTenantCreatedEvent(saved);
    }

    private void publishTenantCreatedEvent(Tenant tenant) {
        TenantCreatedEvent payload = new TenantCreatedEvent(
                tenant.getId(),
                tenant.getName(),
                tenant.getDescription(),
                tenant.getOwnerId(),
                tenant.getMaxUsers(),
                tenant.getAuthenticationEnabled()
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

    private void publishTenantAuthenticationEnabledEvent(Tenant tenant) {
        TenantAuthenticationEnabledEvent payload = new TenantAuthenticationEnabledEvent(
                tenant.getId(),
                tenant.getName(),
                tenant.getOwnerId()
        );

        eventPublisher.publish(
                EventTopics.TENANT_DOMAIN,
                TenantDomainActions.AUTHENTICATION_ENABLED,
                "project-service",
                tenant.getId(),
                tenant.getId(),
                payload,
                java.util.Map.of()
        );
        log.info("Published TenantAuthenticationEnabledEvent for tenant id={}", tenant.getId());
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getDescription(),
                tenant.getOwnerId(),
                tenant.getStatus(),
                tenant.getMaxUsers(),
                tenant.getAuthenticationEnabled(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}

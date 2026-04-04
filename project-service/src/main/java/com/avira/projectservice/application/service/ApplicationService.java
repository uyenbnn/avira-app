package com.avira.projectservice.application.service;

import com.avira.commonlib.constants.ApplicationDomainActions;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.NotFoundException;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.commonlib.messaging.application.ApplicationCreatedEvent;
import com.avira.projectservice.application.dto.ApplicationResponse;
import com.avira.projectservice.application.dto.CreateApplicationRequest;
import com.avira.projectservice.application.dto.UpdateApplicationRequest;
import com.avira.projectservice.application.entity.Application;
import com.avira.projectservice.application.enums.ApplicationStatus;
import com.avira.projectservice.application.repository.ApplicationRepository;
import com.avira.projectservice.tenant.entity.Tenant;
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
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final TenantRepository tenantRepository;
    private final EventPublisher eventPublisher;

    @Transactional
    public ApplicationResponse create(String tenantId, CreateApplicationRequest request, String ownerId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant not found with id: " + tenantId));

        if (!tenant.getOwnerId().equals(ownerId)) {
            throw new com.avira.commonlib.exception.ForbiddenException("You do not own this tenant");
        }

        if (applicationRepository.findByTenantIdAndName(tenantId, request.name()).isPresent()) {
            throw new ConflictException("Application with name '" + request.name() + "' already exists in this tenant");
        }

        String domain = resolveDomain(request.domain(), request.name(), ownerId);

        if (domain != null && !domain.isBlank() && applicationRepository.existsByDomain(domain)) {
            throw new ConflictException("Domain '" + domain + "' is already in use");
        }

        Application app = Application.builder()
                .tenantId(tenantId)
                .name(request.name())
                .kind(request.kind())
                .description(request.description())
                .domain(domain)
                .status(ApplicationStatus.ACTIVE)
                .build();

        Application saved = applicationRepository.save(app);
        log.info("Created application id={} name={} kind={} tenantId={}", saved.getId(), saved.getName(), saved.getKind(), tenantId);

        publishApplicationCreatedEvent(saved, ownerId);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(String id) {
        return applicationRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> findByTenantId(String tenantId, Pageable pageable) {
        return applicationRepository.findByTenantId(tenantId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponse> findAll(Pageable pageable) {
        return applicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public ApplicationResponse update(String id, UpdateApplicationRequest request, String ownerId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        Tenant tenant = tenantRepository.findById(app.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found for application id: " + id));

        if (!tenant.getOwnerId().equals(ownerId)) {
            throw new com.avira.commonlib.exception.ForbiddenException("You do not own this application");
        }

        if (request.name() != null && !request.name().isBlank()) {
            app.setName(request.name());
        }
        if (request.kind() != null) {
            app.setKind(request.kind());
        }
        if (request.description() != null) {
            app.setDescription(request.description());
        }
        if (request.domain() != null) {
            String newDomain = request.domain().isBlank() ? null : request.domain();
            if (newDomain != null && !newDomain.equals(app.getDomain()) && applicationRepository.existsByDomain(newDomain)) {
                throw new ConflictException("Domain '" + newDomain + "' is already in use");
            }
            app.setDomain(newDomain);
        }

        Application updated = applicationRepository.save(app);
        log.info("Updated application id={} name={}", updated.getId(), updated.getName());

        return toResponse(updated);
    }

    @Transactional
    public void changeStatus(String id, ApplicationStatus status, String ownerId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        Tenant tenant = tenantRepository.findById(app.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found for application id: " + id));

        if (!tenant.getOwnerId().equals(ownerId)) {
            throw new com.avira.commonlib.exception.ForbiddenException("You do not own this application");
        }

        app.setStatus(status);
        applicationRepository.save(app);
        log.info("Changed application id={} status={}", id, status);
    }

    @Transactional
    public void delete(String id, String ownerId) {
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Application not found with id: " + id));

        Tenant tenant = tenantRepository.findById(app.getTenantId())
                .orElseThrow(() -> new NotFoundException("Tenant not found for application id: " + id));

        if (!tenant.getOwnerId().equals(ownerId)) {
            throw new com.avira.commonlib.exception.ForbiddenException("You do not own this application");
        }

        app.setStatus(ApplicationStatus.DELETED);
        applicationRepository.save(app);
        log.info("Deleted application id={} name={}", id, app.getName());
    }

    private String resolveDomain(String requestedDomain, String appName, String ownerId) {
        if (requestedDomain != null && !requestedDomain.isBlank()) {
            return requestedDomain;
        }
        // Generate sub-domain: {app-name}.{username}.avira.io
        String slug = appName.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-");
        return slug + "." + ownerId.toLowerCase() + ".avira.io";
    }

    private void publishApplicationCreatedEvent(Application app, String ownerId) {
        ApplicationCreatedEvent payload = new ApplicationCreatedEvent(
                app.getId(),
                app.getTenantId(),
                ownerId,
                app.getName(),
                app.getDomain(),
                app.getKind() != null ? app.getKind().name() : null
        );
        eventPublisher.publish(
                EventTopics.APPLICATION_DOMAIN,
                ApplicationDomainActions.CREATED,
                "project-service",
                app.getId(),
                app.getId(),
                payload,
                java.util.Map.of()
        );
        log.info("Published ApplicationCreatedEvent for application id={}", app.getId());
    }

    private ApplicationResponse toResponse(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getTenantId(),
                app.getName(),
                app.getDomain(),
                app.getKind(),
                app.getDescription(),
                app.getStatus(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}


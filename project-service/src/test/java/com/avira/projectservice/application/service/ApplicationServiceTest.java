package com.avira.projectservice.application.service;

import com.avira.commonlib.constants.ApplicationDomainActions;
import com.avira.commonlib.constants.EventTopics;
import com.avira.commonlib.exception.ConflictException;
import com.avira.commonlib.exception.ForbiddenException;
import com.avira.commonlib.exception.NotFoundException;
import com.avira.commonlib.messaging.EventPublisher;
import com.avira.projectservice.application.dto.ApplicationResponse;
import com.avira.projectservice.application.dto.CreateApplicationRequest;
import com.avira.projectservice.application.dto.UpdateApplicationRequest;
import com.avira.projectservice.application.entity.Application;
import com.avira.projectservice.application.enums.ApplicationKind;
import com.avira.projectservice.application.enums.ApplicationStatus;
import com.avira.projectservice.application.repository.ApplicationRepository;
import com.avira.projectservice.tenant.entity.Tenant;
import com.avira.projectservice.tenant.enums.TenantStatus;
import com.avira.projectservice.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ApplicationService applicationService;

    private Tenant buildTenant(String id, String ownerId) {
        return Tenant.builder()
                .id(id)
                .name("test-workspace")
                .ownerId(ownerId)
                .status(TenantStatus.ACTIVE)
                .maxUsers(100)
                .authenticationEnabled(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Application buildApplication(String id, String tenantId) {
        return Application.builder()
                .id(id)
                .tenantId(tenantId)
                .name("My App")
                .kind(ApplicationKind.ECOMMERCE_APP)
                .description("A test app")
                .domain("myapp.user1.avira.io")
                .status(ApplicationStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    void testCreate_Success() {
        CreateApplicationRequest request = new CreateApplicationRequest(
                "My App", ApplicationKind.ECOMMERCE_APP, "A test app", null);

        Tenant tenant = buildTenant("tenant-1", "user1");
        Application saved = buildApplication("app-1", "tenant-1");

        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));
        when(applicationRepository.findByTenantIdAndName("tenant-1", "My App")).thenReturn(Optional.empty());
        when(applicationRepository.existsByDomain(anyString())).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(saved);

        ApplicationResponse response = applicationService.create("tenant-1", request, "user1");

        assertNotNull(response);
        assertEquals("app-1", response.id());
        assertEquals("tenant-1", response.tenantId());
        verify(applicationRepository).save(any(Application.class));
        verify(eventPublisher).publish(
                eq(EventTopics.APPLICATION_DOMAIN),
                eq(ApplicationDomainActions.CREATED),
                eq("project-service"),
                eq("app-1"),
                eq("app-1"),
                any(),
                any()
        );
    }

    @Test
    void testCreate_TenantNotFound() {
        when(tenantRepository.findById("bad-tenant")).thenReturn(Optional.empty());

        CreateApplicationRequest request = new CreateApplicationRequest(
                "My App", ApplicationKind.PERSONAL_WEB_APP, null, null);

        assertThrows(NotFoundException.class,
                () -> applicationService.create("bad-tenant", request, "user1"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreate_ForbiddenWhenNotOwner() {
        Tenant tenant = buildTenant("tenant-1", "owner1");
        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));

        CreateApplicationRequest request = new CreateApplicationRequest(
                "My App", ApplicationKind.TOOLBOX_WEBAPP, null, null);

        assertThrows(ForbiddenException.class,
                () -> applicationService.create("tenant-1", request, "other-user"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreate_ConflictWhenNameExists() {
        Tenant tenant = buildTenant("tenant-1", "user1");
        Application existing = buildApplication("app-existing", "tenant-1");

        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));
        when(applicationRepository.findByTenantIdAndName("tenant-1", "My App")).thenReturn(Optional.of(existing));

        CreateApplicationRequest request = new CreateApplicationRequest(
                "My App", ApplicationKind.ECOMMERCE_APP, null, null);

        assertThrows(ConflictException.class,
                () -> applicationService.create("tenant-1", request, "user1"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testCreate_ConflictWhenDomainTaken() {
        Tenant tenant = buildTenant("tenant-1", "user1");

        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));
        when(applicationRepository.findByTenantIdAndName("tenant-1", "My App")).thenReturn(Optional.empty());
        when(applicationRepository.existsByDomain("my-custom-domain.com")).thenReturn(true);

        CreateApplicationRequest request = new CreateApplicationRequest(
                "My App", ApplicationKind.ECOMMERCE_APP, null, "my-custom-domain.com");

        assertThrows(ConflictException.class,
                () -> applicationService.create("tenant-1", request, "user1"));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void testFindById_Success() {
        Application app = buildApplication("app-1", "tenant-1");
        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));

        ApplicationResponse response = applicationService.findById("app-1");

        assertNotNull(response);
        assertEquals("app-1", response.id());
        assertEquals(ApplicationKind.ECOMMERCE_APP, response.kind());
    }

    @Test
    void testFindById_NotFound() {
        when(applicationRepository.findById("bad-id")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> applicationService.findById("bad-id"));
    }

    @Test
    void testFindByTenantId() {
        Pageable pageable = PageRequest.of(0, 20);
        Application app = buildApplication("app-1", "tenant-1");
        Page<Application> page = new PageImpl<>(List.of(app), pageable, 1);
        when(applicationRepository.findByTenantId("tenant-1", pageable)).thenReturn(page);

        Page<ApplicationResponse> response = applicationService.findByTenantId("tenant-1", pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testDelete_Success() {
        Application app = buildApplication("app-1", "tenant-1");
        Tenant tenant = buildTenant("tenant-1", "user1");

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));
        when(applicationRepository.save(any(Application.class))).thenReturn(app);

        applicationService.delete("app-1", "user1");

        verify(applicationRepository).save(argThat(a -> a.getStatus() == ApplicationStatus.DELETED));
    }

    @Test
    void testDelete_ForbiddenWhenNotOwner() {
        Application app = buildApplication("app-1", "tenant-1");
        Tenant tenant = buildTenant("tenant-1", "owner1");

        when(applicationRepository.findById("app-1")).thenReturn(Optional.of(app));
        when(tenantRepository.findById("tenant-1")).thenReturn(Optional.of(tenant));

        assertThrows(ForbiddenException.class, () -> applicationService.delete("app-1", "hacker"));
        verify(applicationRepository, never()).save(any());
    }
}


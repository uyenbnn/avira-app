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
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void testCreateTenant_Success() {
        CreateTenantRequest request = new CreateTenantRequest(
                "Test Tenant",
                "A test tenant",
                100
        );

        Tenant savedTenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .description("A test tenant")
                .ownerId("owner-123")
                .status(TenantStatus.ACTIVE)
                .maxUsers(100)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(tenantRepository.findByName("Test Tenant")).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);

        TenantResponse response = tenantService.create(request, "owner-123");

        assertNotNull(response);
        assertEquals("tenant-123", response.id());
        assertEquals("Test Tenant", response.name());
        assertEquals("owner-123", response.ownerId());

        verify(tenantRepository).findByName("Test Tenant");
        verify(tenantRepository).save(any(Tenant.class));
        verify(eventPublisher).publish(
                eq(EventTopics.TENANT_DOMAIN),
                eq(TenantDomainActions.CREATED),
                eq("project-service"),
                eq("tenant-123"),
                eq("tenant-123"),
                any(),
                any()
        );
    }

    @Test
    void testCreateTenant_ConflictException() {
        CreateTenantRequest request = new CreateTenantRequest(
                "Existing Tenant",
                "A test tenant",
                100
        );

        Tenant existingTenant = Tenant.builder()
                .id("tenant-existing")
                .name("Existing Tenant")
                .build();

        when(tenantRepository.findByName("Existing Tenant")).thenReturn(Optional.of(existingTenant));

        assertThrows(ConflictException.class, () -> tenantService.create(request, "owner-123"));
        verify(tenantRepository).findByName("Existing Tenant");
        verify(tenantRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testFindById_Success() {
        Tenant tenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .ownerId("owner-123")
                .status(TenantStatus.ACTIVE)
                .maxUsers(100)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(tenant));

        TenantResponse response = tenantService.findById("tenant-123");

        assertNotNull(response);
        assertEquals("tenant-123", response.id());
        assertEquals("Test Tenant", response.name());
    }

    @Test
    void testFindById_NotFound() {
        when(tenantRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> tenantService.findById("nonexistent"));
    }

    @Test
    void testFindAll() {
        Pageable pageable = PageRequest.of(0, 20);
        Tenant tenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .ownerId("owner-123")
                .build();

        Page<Tenant> page = new PageImpl<>(List.of(tenant), pageable, 1);
        when(tenantRepository.findAll(pageable)).thenReturn(page);

        Page<TenantResponse> response = tenantService.findAll(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void testUpdate_Success() {
        UpdateTenantRequest request = new UpdateTenantRequest(
                "Updated Tenant",
                "Updated description",
                150
        );

        Tenant tenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .description("Original description")
                .ownerId("owner-123")
                .maxUsers(100)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        tenantService.update("tenant-123", request);

        verify(tenantRepository).findById("tenant-123");
        verify(tenantRepository).save(any(Tenant.class));
    }

    @Test
    void testChangeStatus() {
        Tenant tenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        tenantService.changeStatus("tenant-123", TenantStatus.SUSPENDED);

        verify(tenantRepository).findById("tenant-123");
        verify(tenantRepository).save(argThat(t -> t.getStatus() == TenantStatus.SUSPENDED));
    }

    @Test
    void testDelete() {
        Tenant tenant = Tenant.builder()
                .id("tenant-123")
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(tenantRepository.findById("tenant-123")).thenReturn(Optional.of(tenant));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        tenantService.delete("tenant-123");

        verify(tenantRepository).findById("tenant-123");
        verify(tenantRepository).save(argThat(t -> t.getStatus() == TenantStatus.DELETED));
    }
}

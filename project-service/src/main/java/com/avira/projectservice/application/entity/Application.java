package com.avira.projectservice.application.entity;

import com.avira.projectservice.application.enums.ApplicationKind;
import com.avira.projectservice.application.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 64)
    private String id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "domain", length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 50)
    private ApplicationKind kind;

    @Column(name = "description", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ApplicationStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}


package com.avira.iamservice.userservice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "platform_user")
public class PlatformUser {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    protected PlatformUser() {
    }

    public PlatformUser(UUID id, String username, String email, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}


package com.avira.userservice.user.entity;

import com.avira.userservice.user.enums.AuthProvider;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "user_auth_providers",
        indexes = {
                @Index(name = "idx_user_auth_providers_user_id", columnList = "user_id"),
                @Index(name = "idx_user_auth_providers_email", columnList = "email")
        }
)
public class UserAuthProvider {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProvider provider;

    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;

    @Column(name = "email", length = 255)
    private String email;

    @PrePersist
    protected void ensureId() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}

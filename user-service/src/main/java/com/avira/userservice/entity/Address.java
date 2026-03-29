package com.avira.userservice.entity;

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
        name = "addresses",
        indexes = {
                @Index(name = "idx_addresses_user_id", columnList = "user_id")
        }
)
public class Address {

    @Id
    @Column(name = "id", updatable = false, nullable = false, length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", length = 150)
    private String receiverName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "ward", length = 100)
    private String ward;

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @PrePersist
    protected void ensureId() {
        if (this.id == null || this.id.isBlank()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}

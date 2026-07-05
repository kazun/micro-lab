package com.peap.product.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * A rateable public entity: a product, restaurant, government service, political
 * party, or any other category listed in the AID's target domains (section 4).
 */
@Entity
@Table(name = "public_entities")
public class PublicEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PublicEntity() {
    }

    public PublicEntity(UUID id, String name, String category, String description, EntityStatus status, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public EntityStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

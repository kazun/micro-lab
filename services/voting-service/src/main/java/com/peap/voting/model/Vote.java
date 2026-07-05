package com.peap.voting.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

/**
 * One row per (entityId, userId) pair - enforces the AID rule "one user vote
 * per entity version" (section 6.4). Re-votes update the existing row.
 */
@Entity
@Table(name = "votes", uniqueConstraints = @UniqueConstraint(columnNames = {"entity_id", "user_id"}))
public class Vote {

    @Id
    private UUID id;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private int value;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Vote() {
    }

    public Vote(UUID id, UUID entityId, UUID userId, int value, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.entityId = entityId;
        this.userId = userId;
        this.value = value;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

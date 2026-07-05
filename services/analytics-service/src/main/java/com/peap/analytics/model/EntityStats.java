package com.peap.analytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Denormalized read model for one public entity, incrementally updated as
 * entity-created / vote-submitted / review-submitted events arrive. Writes go
 * through {@link com.peap.analytics.repository.EntityStatsRepository}'s
 * native upserts (three different topic-partition threads can touch the same
 * entity_id concurrently, so plain JPA read-then-save would race); this class
 * is the read side only. Vote updates (re-votes) are counted as new data
 * points rather than replacing the prior value - acceptable for the MVP,
 * revisit if exact averages matter.
 *
 * @author kazun
 */
@Entity
@Table(name = "entity_stats")
public class EntityStats {

    @Id
    private UUID entityId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "vote_count", nullable = false)
    private long voteCount;

    @Column(name = "vote_sum", nullable = false)
    private long voteSum;

    @Column(name = "review_count", nullable = false)
    private long reviewCount;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected EntityStats() {
    }

    public UUID getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public long getVoteSum() {
        return voteSum;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public double averageScore() {
        return voteCount == 0 ? 0.0 : (double) voteSum / voteCount;
    }
}

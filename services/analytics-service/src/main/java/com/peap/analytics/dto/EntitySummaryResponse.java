package com.peap.analytics.dto;

import com.peap.analytics.model.EntityStats;
import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record EntitySummaryResponse(
        UUID entityId,
        String name,
        String category,
        long voteCount,
        double averageScore,
        long reviewCount,
        Instant updatedAt) {

    public static EntitySummaryResponse from(EntityStats stats) {
        return new EntitySummaryResponse(
                stats.getEntityId(),
                stats.getName(),
                stats.getCategory(),
                stats.getVoteCount(),
                stats.averageScore(),
                stats.getReviewCount(),
                stats.getUpdatedAt());
    }
}

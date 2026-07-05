package com.peap.review.dto;

import com.peap.review.model.Review;
import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record ReviewResponse(UUID id, UUID entityId, UUID userId, String text, String status, Instant createdAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getEntityId(),
                review.getUserId(),
                review.getText(),
                review.getStatus().name(),
                review.getCreatedAt());
    }
}

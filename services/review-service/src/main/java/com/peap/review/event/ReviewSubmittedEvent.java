package com.peap.review.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record ReviewSubmittedEvent(UUID reviewId, UUID entityId, UUID userId, Instant submittedAt) {
}

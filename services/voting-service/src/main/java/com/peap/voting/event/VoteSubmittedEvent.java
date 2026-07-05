package com.peap.voting.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record VoteSubmittedEvent(UUID entityId, UUID userId, int value, Instant submittedAt) {
}

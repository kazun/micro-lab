package com.peap.identity.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record UserCreatedEvent(UUID userId, String email, String role, Instant createdAt) {
}

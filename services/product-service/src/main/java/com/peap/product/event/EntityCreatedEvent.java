package com.peap.product.event;

import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record EntityCreatedEvent(UUID entityId, String name, String category, Instant createdAt) {
}

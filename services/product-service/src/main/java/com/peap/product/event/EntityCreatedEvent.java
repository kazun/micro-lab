package com.peap.product.event;

import java.time.Instant;
import java.util.UUID;

public record EntityCreatedEvent(UUID entityId, String name, String category, Instant createdAt) {
}

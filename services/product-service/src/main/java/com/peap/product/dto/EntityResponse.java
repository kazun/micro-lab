package com.peap.product.dto;

import com.peap.product.model.PublicEntity;
import java.time.Instant;
import java.util.UUID;

public record EntityResponse(
        UUID id, String name, String category, String description, String status, Instant createdAt) {

    public static EntityResponse from(PublicEntity entity) {
        return new EntityResponse(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getStatus().name(),
                entity.getCreatedAt());
    }
}

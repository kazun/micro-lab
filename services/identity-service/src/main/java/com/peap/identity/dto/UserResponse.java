package com.peap.identity.dto;

import com.peap.identity.model.User;
import java.time.Instant;
import java.util.UUID;

/**
 * @author kazun
 */
public record UserResponse(UUID id, String email, String role, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole().name(), user.getCreatedAt());
    }
}

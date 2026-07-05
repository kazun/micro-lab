package com.peap.identity.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInMs) {

    public static AuthResponse bearer(String accessToken, long expiresInMs) {
        return new AuthResponse(accessToken, "Bearer", expiresInMs);
    }
}

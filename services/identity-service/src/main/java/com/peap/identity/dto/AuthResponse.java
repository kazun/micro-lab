package com.peap.identity.dto;

/**
 * @author kazun
 */
public record AuthResponse(String accessToken, String tokenType, long expiresInMs) {

    public static AuthResponse bearer(String accessToken, long expiresInMs) {
        return new AuthResponse(accessToken, "Bearer", expiresInMs);
    }
}

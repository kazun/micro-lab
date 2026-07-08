package com.peap.analytics.dto;

/**
 * @author kazun
 */
public record PlatformSummaryResponse(
        long usersRegistered, long entitiesCreated, long votesCast, long reviewsSubmitted, long apiRequests) {
}

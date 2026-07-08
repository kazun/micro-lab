package com.peap.analytics.dto;

import com.peap.analytics.model.ApiEndpointStats;
import java.time.Instant;

/**
 * @author kazun
 */
public record EndpointStatsResponse(
        String endpoint, long requestCount, double avgDurationMs, long errorCount, Instant lastSeen) {

    public static EndpointStatsResponse from(ApiEndpointStats stats) {
        return new EndpointStatsResponse(
                stats.getEndpoint(),
                stats.getRequestCount(),
                stats.averageDurationMs(),
                stats.getErrorCount(),
                stats.getLastSeen());
    }
}

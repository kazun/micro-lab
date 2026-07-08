package com.peap.analytics.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Rolling per-endpoint traffic stats fed by the gateway's api-request
 * events. Keyed by "METHOD normalized-path" (path IDs collapsed to {id})
 * so cardinality stays bounded by the API surface, not by traffic. Writes
 * go through native upserts in the repository - api-request is the
 * highest-volume topic, so read-then-save would race constantly.
 *
 * @author kazun
 */
@Entity
@Table(name = "api_endpoint_stats")
public class ApiEndpointStats {

    @Id
    @Column(name = "endpoint")
    private String endpoint;

    @Column(name = "request_count", nullable = false)
    private long requestCount;

    @Column(name = "total_duration_ms", nullable = false)
    private long totalDurationMs;

    @Column(name = "error_count", nullable = false)
    private long errorCount;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    protected ApiEndpointStats() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public long getTotalDurationMs() {
        return totalDurationMs;
    }

    public long getErrorCount() {
        return errorCount;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public double averageDurationMs() {
        return requestCount == 0 ? 0.0 : (double) totalDurationMs / requestCount;
    }
}

package com.peap.gateway.event;

import java.time.Instant;

/**
 * One event per API call passing through the gateway, published to the
 * api-request Kafka topic for traffic-level fraud analysis (request
 * flooding, scraping, credential stuffing). This is the only place in the
 * platform that sees client IP and user agent.
 *
 * @author kazun
 */
public record ApiRequestEvent(
        String method,
        String path,
        int status,
        String clientIp,
        String userAgent,
        long durationMs,
        Instant timestamp) {
}

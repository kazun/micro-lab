package com.peap.analytics.kafka;

import com.peap.analytics.service.AnalyticsService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes the domain events published by identity/product/voting/review
 * services. Payloads are read as a generic {@code Map} rather than the
 * producer's event classes - this service only depends on the JSON shape of
 * each topic, not on the producing module's Java types.
 *
 * @author kazun
 */
@Component
public class AnalyticsEventListener {

    private final AnalyticsService analyticsService;

    public AnalyticsEventListener(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(topics = "user-created")
    public void onUserCreated(Map<String, Object> event) {
        analyticsService.recordUserCreated();
    }

    @KafkaListener(topics = "entity-created")
    public void onEntityCreated(Map<String, Object> event) {
        analyticsService.recordEntityCreated(
                uuid(event, "entityId"),
                (String) event.get("name"),
                (String) event.get("category"),
                instant(event, "createdAt"));
    }

    @KafkaListener(topics = "vote-submitted")
    public void onVoteSubmitted(Map<String, Object> event) {
        analyticsService.recordVoteSubmitted(
                uuid(event, "entityId"),
                ((Number) event.get("value")).intValue(),
                instant(event, "submittedAt"));
    }

    @KafkaListener(topics = "review-submitted")
    public void onReviewSubmitted(Map<String, Object> event) {
        analyticsService.recordReviewSubmitted(uuid(event, "entityId"), instant(event, "submittedAt"));
    }

    private static UUID uuid(Map<String, Object> event, String field) {
        return UUID.fromString((String) event.get(field));
    }

    /**
     * Producers' JsonSerializer instances use their own Jackson config, which
     * may render Instant as an ISO-8601 string or as a numeric epoch-seconds
     * timestamp depending on ObjectMapper defaults - accept either form.
     */
    private static Instant instant(Map<String, Object> event, String field) {
        Object raw = event.get(field);
        if (raw instanceof String s) {
            return Instant.parse(s);
        }
        if (raw instanceof Number n) {
            double epochSeconds = n.doubleValue();
            long seconds = (long) epochSeconds;
            long nanos = Math.round((epochSeconds - seconds) * 1_000_000_000L);
            return Instant.ofEpochSecond(seconds, nanos);
        }
        throw new IllegalArgumentException(
                "unsupported timestamp type for field " + field + ": " + (raw == null ? "null" : raw.getClass()));
    }
}

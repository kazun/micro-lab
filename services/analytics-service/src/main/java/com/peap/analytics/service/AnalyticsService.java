package com.peap.analytics.service;

import com.peap.analytics.dto.EntitySummaryResponse;
import com.peap.analytics.dto.PlatformSummaryResponse;
import com.peap.analytics.model.EntityStats;
import com.peap.analytics.model.PlatformCounter;
import com.peap.analytics.repository.EntityStatsRepository;
import com.peap.analytics.repository.PlatformCounterRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consumes domain events (via {@link com.peap.analytics.kafka.AnalyticsEventListener})
 * into a denormalized read model, and serves it back out over REST.
 *
 * @author kazun
 */
@Service
public class AnalyticsService {

    private static final String METRIC_USERS_REGISTERED = "users_registered";
    private static final String METRIC_ENTITIES_CREATED = "entities_created";
    private static final String METRIC_VOTES_CAST = "votes_cast";
    private static final String METRIC_REVIEWS_SUBMITTED = "reviews_submitted";

    private final EntityStatsRepository entityStatsRepository;
    private final PlatformCounterRepository platformCounterRepository;

    public AnalyticsService(
            EntityStatsRepository entityStatsRepository, PlatformCounterRepository platformCounterRepository) {
        this.entityStatsRepository = entityStatsRepository;
        this.platformCounterRepository = platformCounterRepository;
    }

    @Transactional
    public void recordUserCreated() {
        incrementCounter(METRIC_USERS_REGISTERED);
    }

    @Transactional
    public void recordEntityCreated(UUID entityId, String name, String category, Instant createdAt) {
        entityStatsRepository.upsertEntityCreated(entityId, name, category, createdAt);
        incrementCounter(METRIC_ENTITIES_CREATED);
    }

    @Transactional
    public void recordVoteSubmitted(UUID entityId, int value, Instant submittedAt) {
        entityStatsRepository.upsertVoteSubmitted(entityId, value, submittedAt);
        incrementCounter(METRIC_VOTES_CAST);
    }

    @Transactional
    public void recordReviewSubmitted(UUID entityId, Instant submittedAt) {
        entityStatsRepository.upsertReviewSubmitted(entityId, submittedAt);
        incrementCounter(METRIC_REVIEWS_SUBMITTED);
    }

    public EntitySummaryResponse getEntitySummary(UUID entityId) {
        return entityStatsRepository.findById(entityId)
                .map(EntitySummaryResponse::from)
                .orElseThrow(() -> new NoSuchElementException("no analytics recorded for entity: " + entityId));
    }

    public PlatformSummaryResponse getPlatformSummary() {
        return new PlatformSummaryResponse(
                counterValue(METRIC_USERS_REGISTERED),
                counterValue(METRIC_ENTITIES_CREATED),
                counterValue(METRIC_VOTES_CAST),
                counterValue(METRIC_REVIEWS_SUBMITTED));
    }

    public List<EntitySummaryResponse> getLeaderboard(String category, int limit) {
        List<EntityStats> top = category == null
                ? entityStatsRepository.findTopByVoteCount(PageRequest.of(0, limit))
                : entityStatsRepository.findTopByCategory(category, PageRequest.of(0, limit));
        return top.stream().map(EntitySummaryResponse::from).toList();
    }

    private void incrementCounter(String metricName) {
        PlatformCounter counter = platformCounterRepository.findById(metricName)
                .orElseGet(() -> new PlatformCounter(metricName));
        counter.increment();
        platformCounterRepository.save(counter);
    }

    private long counterValue(String metricName) {
        return platformCounterRepository.findById(metricName)
                .map(PlatformCounter::getValue)
                .orElse(0L);
    }
}

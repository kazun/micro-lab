package com.peap.analytics.repository;

import com.peap.analytics.model.ApiEndpointStats;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author kazun
 */
public interface ApiEndpointStatsRepository extends JpaRepository<ApiEndpointStats, String> {

    @Query("select s from ApiEndpointStats s order by s.requestCount desc")
    List<ApiEndpointStats> findTopByRequestCount(Pageable pageable);

    @Modifying
    @Query(value = """
            insert into api_endpoint_stats (endpoint, request_count, total_duration_ms, error_count, last_seen)
            values (:endpoint, 1, :durationMs, :errorDelta, :seenAt)
            on conflict (endpoint) do update set
                request_count = api_endpoint_stats.request_count + 1,
                total_duration_ms = api_endpoint_stats.total_duration_ms + :durationMs,
                error_count = api_endpoint_stats.error_count + :errorDelta,
                last_seen = :seenAt
            """, nativeQuery = true)
    void upsertRequest(
            @Param("endpoint") String endpoint,
            @Param("durationMs") long durationMs,
            @Param("errorDelta") int errorDelta,
            @Param("seenAt") Instant seenAt);
}

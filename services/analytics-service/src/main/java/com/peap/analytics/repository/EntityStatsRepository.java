package com.peap.analytics.repository;

import com.peap.analytics.model.EntityStats;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * entity-created, vote-submitted, and review-submitted are consumed by three
 * separate partition threads and can race on the same entity_id, so writes
 * use atomic Postgres upserts rather than JPA read-then-save.
 *
 * @author kazun
 */
public interface EntityStatsRepository extends JpaRepository<EntityStats, UUID> {

    @Query("select e from EntityStats e order by e.voteCount desc")
    List<EntityStats> findTopByVoteCount(Pageable pageable);

    @Query("select e from EntityStats e where e.category = :category order by e.voteCount desc")
    List<EntityStats> findTopByCategory(String category, Pageable pageable);

    @Modifying
    @Query(value = """
            insert into entity_stats (entity_id, name, category, vote_count, vote_sum, review_count, updated_at)
            values (:entityId, :name, :category, 0, 0, 0, :updatedAt)
            on conflict (entity_id) do update set
                name = excluded.name,
                category = excluded.category,
                updated_at = excluded.updated_at
            """, nativeQuery = true)
    void upsertEntityCreated(
            @Param("entityId") UUID entityId,
            @Param("name") String name,
            @Param("category") String category,
            @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query(value = """
            insert into entity_stats (entity_id, name, category, vote_count, vote_sum, review_count, updated_at)
            values (:entityId, 'unknown', 'unknown', 1, :value, 0, :updatedAt)
            on conflict (entity_id) do update set
                vote_count = entity_stats.vote_count + 1,
                vote_sum = entity_stats.vote_sum + :value,
                updated_at = :updatedAt
            """, nativeQuery = true)
    void upsertVoteSubmitted(
            @Param("entityId") UUID entityId, @Param("value") int value, @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query(value = """
            insert into entity_stats (entity_id, name, category, vote_count, vote_sum, review_count, updated_at)
            values (:entityId, 'unknown', 'unknown', 0, 0, 1, :updatedAt)
            on conflict (entity_id) do update set
                review_count = entity_stats.review_count + 1,
                updated_at = :updatedAt
            """, nativeQuery = true)
    void upsertReviewSubmitted(@Param("entityId") UUID entityId, @Param("updatedAt") Instant updatedAt);
}

package com.peap.voting.repository;

import com.peap.voting.model.Vote;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author kazun
 */
public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByEntityIdAndUserId(UUID entityId, UUID userId);

    long countByEntityId(UUID entityId);

    @Query("select coalesce(avg(v.value), 0) from Vote v where v.entityId = :entityId")
    double averageValueByEntityId(UUID entityId);
}

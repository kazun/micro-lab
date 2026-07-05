package com.peap.voting.service;

import com.peap.voting.dto.CastVoteRequest;
import com.peap.voting.dto.ScoreResponse;
import com.peap.voting.event.VoteEventPublisher;
import com.peap.voting.model.Vote;
import com.peap.voting.repository.VoteRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final VoteEventPublisher eventPublisher;

    public VoteService(VoteRepository voteRepository, VoteEventPublisher eventPublisher) {
        this.voteRepository = voteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    @CacheEvict(cacheNames = "entity-scores", key = "#request.entityId()")
    public Vote castVote(CastVoteRequest request) {
        Instant now = Instant.now();
        Vote vote = voteRepository.findByEntityIdAndUserId(request.entityId(), request.userId())
                .map(existing -> {
                    existing.setValue(request.value());
                    existing.setUpdatedAt(now);
                    return existing;
                })
                .orElseGet(() -> new Vote(
                        UUID.randomUUID(), request.entityId(), request.userId(), request.value(), now, now));
        voteRepository.save(vote);
        eventPublisher.publishVoteSubmitted(vote);
        return vote;
    }

    @Cacheable(cacheNames = "entity-scores", key = "#entityId")
    public ScoreResponse getScore(UUID entityId) {
        double average = voteRepository.averageValueByEntityId(entityId);
        long count = voteRepository.countByEntityId(entityId);
        return new ScoreResponse(entityId, average, count);
    }
}

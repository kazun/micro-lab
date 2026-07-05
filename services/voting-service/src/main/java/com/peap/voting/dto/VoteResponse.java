package com.peap.voting.dto;

import com.peap.voting.model.Vote;
import java.time.Instant;
import java.util.UUID;

public record VoteResponse(UUID id, UUID entityId, UUID userId, int value, Instant updatedAt) {

    public static VoteResponse from(Vote vote) {
        return new VoteResponse(vote.getId(), vote.getEntityId(), vote.getUserId(), vote.getValue(), vote.getUpdatedAt());
    }
}

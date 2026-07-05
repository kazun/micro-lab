package com.peap.voting.dto;

import java.util.UUID;

public record ScoreResponse(UUID entityId, double averageScore, long voteCount) {
}

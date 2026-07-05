package com.peap.voting.dto;

import java.util.UUID;

/**
 * @author kazun
 */
public record ScoreResponse(UUID entityId, double averageScore, long voteCount) {
}

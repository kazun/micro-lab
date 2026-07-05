package com.peap.voting.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * @author kazun
 */
public record CastVoteRequest(

        @NotNull UUID entityId,

        @NotNull UUID userId,

        @Min(1) @Max(5) int value
) {
}

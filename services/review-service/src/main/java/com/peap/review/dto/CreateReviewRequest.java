package com.peap.review.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * @author kazun
 */
public record CreateReviewRequest(

        @NotNull UUID entityId,

        @NotNull UUID userId,

        @NotBlank @Size(max = 4000) String text
) {
}

package com.peap.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author kazun
 */
public record CreateEntityRequest(

        @NotBlank String name,

        @NotBlank String category,

        @Size(max = 2000) String description
) {
}

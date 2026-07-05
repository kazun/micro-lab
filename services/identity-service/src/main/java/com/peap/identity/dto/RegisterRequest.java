package com.peap.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @author kazun
 */
public record RegisterRequest(

        @NotBlank @Email String email,

        @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password
) {
}

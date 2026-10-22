package com.authguard.ai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerificationRequest(
        @NotBlank @Email @Size(max = 254) String account,
        @NotBlank @Pattern(regexp = "^[A-Z2-7]{16,64}$", message = "secret must be Base32 (A-Z, 2-7), 16-64 chars") String secret,
        @NotBlank @Pattern(regexp = "^\\d{6}$", message = "token must be exactly 6 digits") String token
) {
}

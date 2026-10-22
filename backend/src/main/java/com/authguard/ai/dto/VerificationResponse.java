package com.authguard.ai.dto;

public record VerificationResponse(boolean valid, double riskScore, String message) {
}

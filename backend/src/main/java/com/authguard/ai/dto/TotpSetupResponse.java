package com.authguard.ai.dto;

public record TotpSetupResponse(String secret, String qrUri) {
}

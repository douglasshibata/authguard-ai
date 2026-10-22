package com.authguard.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Cors cors,
        AiEngine aiEngine,
        RateLimit rateLimit
) {
    public record Cors(List<String> allowedOrigins) {}

    public record AiEngine(String baseUrl, long timeoutMs, int highRiskThreshold) {}

    public record RateLimit(int capacity, int refillTokens, long refillPeriodSeconds) {}
}

package com.authguard.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RiskAssessment(
        @JsonProperty("risk_score") double riskScore,
        @JsonProperty("high_risk") boolean highRisk,
        @JsonProperty("anomalies") List<String> anomalies
) {

    public static RiskAssessment unknown() {
        return new RiskAssessment(-1.0, false, List.of("ai-engine-unavailable"));
    }
}

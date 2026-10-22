package com.authguard.ai.service;

import com.authguard.ai.dto.RiskAssessment;
import com.authguard.ai.dto.RiskContext;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskEngineClientTest {

    private WireMockServer wireMock;
    private RiskEngineClient client;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(options().dynamicPort());
        wireMock.start();
        WebClient webClient = WebClient.builder().baseUrl(wireMock.baseUrl()).build();
        client = new RiskEngineClient(webClient);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void assess_success_returnsParsedRisk() {
        wireMock.stubFor(post(urlEqualTo("/analyze")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"risk_score\":42.5,\"high_risk\":false,\"anomalies\":[]}")));

        RiskAssessment result = client.assess(new RiskContext("u@example.com", "1.2.3.4", "UA"));

        assertEquals(42.5, result.riskScore());
        assertEquals(false, result.highRisk());
    }

    @Test
    void assess_serverError_fallsBackToUnknown() {
        wireMock.stubFor(post(urlEqualTo("/analyze")).willReturn(aResponse().withStatus(503)));

        RiskAssessment result = client.assess(new RiskContext("u@example.com", "1.2.3.4", "UA"));

        assertEquals(-1.0, result.riskScore());
        assertTrue(result.anomalies().contains("ai-engine-unavailable"));
    }

    @Test
    void assess_timeout_fallsBackToUnknown() {
        wireMock.stubFor(post(urlEqualTo("/analyze")).willReturn(aResponse()
                .withFixedDelay(3000)
                .withBody("{\"risk_score\":1,\"high_risk\":false,\"anomalies\":[]}")));

        RiskAssessment result = client.assess(new RiskContext("u@example.com", "1.2.3.4", "UA"));

        assertEquals(-1.0, result.riskScore());
    }
}

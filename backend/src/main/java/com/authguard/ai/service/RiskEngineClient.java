package com.authguard.ai.service;

import com.authguard.ai.dto.RiskAssessment;
import com.authguard.ai.dto.RiskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class RiskEngineClient {

    private static final Logger log = LoggerFactory.getLogger(RiskEngineClient.class);
    private static final Duration BLOCK_TIMEOUT = Duration.ofMillis(900);

    private final WebClient webClient;

    public RiskEngineClient(WebClient aiEngineWebClient) {
        this.webClient = aiEngineWebClient;
    }

    @Cacheable(value = "riskAssessments", key = "#ctx.account() + ':' + (#ctx.ip() == null ? '-' : #ctx.ip())", unless = "#result.riskScore() < 0")
    public RiskAssessment assessCached(RiskContext ctx) {
        return assess(ctx);
    }

    public RiskAssessment assess(RiskContext ctx) {
        try {
            return webClient.post()
                    .uri("/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                            "account", ctx.account(),
                            "ip", ctx.ip() == null ? "" : ctx.ip(),
                            "user_agent", ctx.userAgent() == null ? "" : ctx.userAgent()
                    ))
                    .retrieve()
                    .bodyToMono(RiskAssessment.class)
                    .blockOptional(BLOCK_TIMEOUT)
                    .orElseGet(() -> {
                        log.warn("ai-engine returned empty body, falling back to unknown risk");
                        return RiskAssessment.unknown();
                    });
        } catch (Exception e) {
            log.warn("ai-engine call failed ({}), falling back to unknown risk", e.getClass().getSimpleName());
            return RiskAssessment.unknown();
        }
    }
}

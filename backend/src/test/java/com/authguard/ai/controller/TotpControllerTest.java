package com.authguard.ai.controller;

import com.authguard.ai.config.AppProperties;
import com.authguard.ai.dto.RiskAssessment;
import com.authguard.ai.dto.RiskContext;
import com.authguard.ai.service.RiskEngineClient;
import com.authguard.ai.service.TotpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TotpController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@Import({TotpControllerTest.MockConfig.class, GlobalExceptionHandler.class})
class TotpControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TotpService totpService;

    @MockitoBean
    RiskEngineClient riskEngineClient;

    @BeforeEach
    void setUp() {
        when(totpService.generateSecret()).thenReturn("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP");
        when(totpService.generateQrCodeUri(any(), any(), any())).thenReturn("otpauth://totp/x");
    }

    @Test
    void setup_returnsSecretAndQr() throws Exception {
        mockMvc.perform(get("/api/totp/setup").param("account", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.secret").value("JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP"))
                .andExpect(jsonPath("$.qrUri").value("otpauth://totp/x"));
    }

    @Test
    void setup_invalidEmail_returns400() throws Exception {
        mockMvc.perform(get("/api/totp/setup").param("account", "not-an-email"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_validToken_lowRisk_returnsOk() throws Exception {
        when(totpService.verifyToken(any(), any())).thenReturn(true);
        when(riskEngineClient.assessCached(any(RiskContext.class)))
                .thenReturn(new RiskAssessment(10.0, false, List.of()));

        String body = """
                {"account":"user@example.com","secret":"JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP","token":"123456"}
                """;

        mockMvc.perform(post("/api/totp/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.riskScore").value(10.0));
    }

    @Test
    void verify_validToken_highRisk_blocked() throws Exception {
        when(totpService.verifyToken(any(), any())).thenReturn(true);
        when(riskEngineClient.assessCached(any(RiskContext.class)))
                .thenReturn(new RiskAssessment(95.0, true, List.of("unusual_ip")));

        String body = """
                {"account":"user@example.com","secret":"JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP","token":"123456"}
                """;

        mockMvc.perform(post("/api/totp/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("High risk detected. Step-up authentication required."));
    }

    @Test
    void verify_malformedToken_returns400() throws Exception {
        String body = """
                {"account":"user@example.com","secret":"JBSWY3DPEHPK3PXPJBSWY3DPEHPK3PXP","token":"abc"}
                """;
        mockMvc.perform(post("/api/totp/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verify_malformedSecret_returns400() throws Exception {
        String body = """
                {"account":"user@example.com","secret":"short","token":"123456"}
                """;
        mockMvc.perform(post("/api/totp/verify").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        AppProperties appProperties() {
            return new AppProperties(
                    new AppProperties.Cors(List.of("http://localhost:4200")),
                    new AppProperties.AiEngine("http://localhost:8000", 800, 80),
                    new AppProperties.RateLimit(10, 10, 60));
        }
    }
}

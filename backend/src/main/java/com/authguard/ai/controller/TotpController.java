package com.authguard.ai.controller;

import com.authguard.ai.config.AppProperties;
import com.authguard.ai.dto.RiskAssessment;
import com.authguard.ai.dto.RiskContext;
import com.authguard.ai.dto.TotpSetupResponse;
import com.authguard.ai.dto.VerificationRequest;
import com.authguard.ai.dto.VerificationResponse;
import com.authguard.ai.service.AuditService;
import com.authguard.ai.service.RiskEngineClient;
import com.authguard.ai.service.TotpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/totp")
@Validated
public class TotpController {

    private static final String ISSUER = "AuthGuardAI";

    private final TotpService totpService;
    private final RiskEngineClient riskEngineClient;
    private final AuditService auditService;
    private final int highRiskThreshold;

    public TotpController(TotpService totpService, RiskEngineClient riskEngineClient, AuditService auditService, AppProperties props) {
        this.totpService = totpService;
        this.riskEngineClient = riskEngineClient;
        this.auditService = auditService;
        this.highRiskThreshold = props.aiEngine().highRiskThreshold();
    }

    @GetMapping("/setup")
    public TotpSetupResponse setup(@RequestParam @NotBlank @Email @Size(max = 254) String account, HttpServletRequest httpRequest) {
        String secret = totpService.generateSecret();
        String qrUri = totpService.generateQrCodeUri(secret, account, ISSUER);
        
        auditService.log(account, "SETUP", true, null, resolveClientIp(httpRequest), httpRequest.getHeader("User-Agent"), "New TOTP secret generated");
        
        return new TotpSetupResponse(secret, qrUri);
    }

    @PostMapping("/verify")
    public VerificationResponse verify(@Valid @RequestBody VerificationRequest request, HttpServletRequest httpRequest) {
        boolean totpValid = totpService.verifyToken(request.secret(), request.token());

        String ip = resolveClientIp(httpRequest);
        String ua = truncate(httpRequest.getHeader("User-Agent"), 512);

        RiskContext ctx = new RiskContext(request.account(), ip, ua);
        RiskAssessment risk = riskEngineClient.assessCached(ctx);

        boolean highRisk = risk.riskScore() >= 0 && risk.riskScore() >= highRiskThreshold;
        boolean authorized = totpValid && !highRisk;

        String message;
        if (!totpValid) {
            message = "Invalid token";
        } else if (highRisk) {
            message = "High risk detected. Step-up authentication required.";
        } else {
            message = null;
        }

        auditService.log(request.account(), "VERIFY", authorized, risk.riskScore(), ip, ua, message);

        return new VerificationResponse(authorized, risk.riskScore(), message);
    }

    private String resolveClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return req.getRemoteAddr();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}

package com.authguard.ai.service;

import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void generateSecret_returnsBase32_160bit() {
        String secret = totpService.generateSecret();
        assertNotNull(secret);
        assertTrue(secret.matches("^[A-Z2-7]+=*$"), "must be Base32: " + secret);
        // 20 bytes (160 bits) Base32-encoded => 32 chars
        assertEquals(32, secret.length());
    }

    @Test
    void generateSecret_isUnique() {
        String a = totpService.generateSecret();
        String b = totpService.generateSecret();
        assertFalse(a.equals(b), "secrets must differ between calls");
    }

    @Test
    void generateQrCodeUri_urlEncodesAccountAndIssuer() {
        String secret = "JBSWY3DPEHPK3PXP";
        String uri = totpService.generateQrCodeUri(secret, "test+user@example.com", "Auth Guard");
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertTrue(uri.contains("secret=JBSWY3DPEHPK3PXP"));
        String decoded = URLDecoder.decode(uri, StandardCharsets.UTF_8);
        assertTrue(decoded.contains("Auth Guard:test+user@example.com"), "decoded label mismatch: " + decoded);
    }

    @Test
    void verifyToken_currentWindow_returnsTrue() {
        String secret = totpService.generateSecret();
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        String token = totpService.generateTotp(secret, timeIndex);
        assertTrue(totpService.verifyToken(secret, token));
    }

    @Test
    void verifyToken_previousWindowWithinDrift_returnsTrue() {
        String secret = totpService.generateSecret();
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        String token = totpService.generateTotp(secret, timeIndex - 1);
        assertTrue(totpService.verifyToken(secret, token), "must accept ±1 window");
    }

    @Test
    void verifyToken_outsideDriftWindow_returnsFalse() {
        String secret = totpService.generateSecret();
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        String token = totpService.generateTotp(secret, timeIndex - 5);
        assertFalse(totpService.verifyToken(secret, token));
    }

    @Test
    void verifyToken_wrongToken_returnsFalse() {
        String secret = totpService.generateSecret();
        assertFalse(totpService.verifyToken(secret, "000000"));
    }

    @Test
    void verifyToken_nullArguments_returnsFalse() {
        assertFalse(totpService.verifyToken(null, "123456"));
        assertFalse(totpService.verifyToken("JBSWY3DPEHPK3PXP", null));
    }

    @Test
    void generateTotp_isSixDigits() {
        String token = totpService.generateTotp("JBSWY3DPEHPK3PXP", 0L);
        assertTrue(token.matches("^\\d{6}$"), "expected 6 digits, got " + token);
    }
}

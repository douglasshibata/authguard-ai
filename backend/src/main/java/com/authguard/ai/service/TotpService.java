package com.authguard.ai.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Service
public class TotpService {

    private static final int SECRET_SIZE = 20;
    private static final int TIME_STEP_SECONDS = 30;
    private static final int TOTP_LENGTH = 6;
    private static final int CLOCK_DRIFT_WINDOWS = 1;
    private static final String HMAC_ALGORITHM = "HmacSHA1";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base32 BASE32 = new Base32();

    public String generateSecret() {
        byte[] bytes = new byte[SECRET_SIZE];
        SECURE_RANDOM.nextBytes(bytes);
        return BASE32.encodeToString(bytes);
    }

    public String generateQrCodeUri(String secret, String account, String issuer) {
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedAccount = URLEncoder.encode(account, StandardCharsets.UTF_8);
        String encodedSecret = URLEncoder.encode(secret, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=%d&period=%d",
                encodedIssuer, encodedAccount, encodedSecret, encodedIssuer, TOTP_LENGTH, TIME_STEP_SECONDS);
    }

    public boolean verifyToken(String secret, String token) {
        if (secret == null || token == null) {
            return false;
        }
        long timeIndex = System.currentTimeMillis() / 1000 / TIME_STEP_SECONDS;
        byte[] tokenBytes = token.getBytes(StandardCharsets.US_ASCII);
        boolean match = false;
        for (int i = -CLOCK_DRIFT_WINDOWS; i <= CLOCK_DRIFT_WINDOWS; i++) {
            String candidate = generateTotp(secret, timeIndex + i);
            byte[] candidateBytes = candidate.getBytes(StandardCharsets.US_ASCII);
            if (MessageDigest.isEqual(candidateBytes, tokenBytes)) {
                match = true;
            }
        }
        return match;
    }

    public String generateTotp(String secret, long timeIndex) {
        byte[] key = BASE32.decode(secret);
        byte[] data = ByteBuffer.allocate(Long.BYTES).putLong(timeIndex).array();

        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;
            for (int i = 0; i < 4; i++) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }
            truncatedHash &= 0x7FFFFFFFL;
            truncatedHash %= (long) Math.pow(10, TOTP_LENGTH);
            return String.format("%0" + TOTP_LENGTH + "d", truncatedHash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Error generating TOTP", e);
        }
    }
}

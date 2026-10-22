package com.authguard.ai.dto;

import java.io.Serializable;

public record RiskContext(String account, String ip, String userAgent) implements Serializable {
}

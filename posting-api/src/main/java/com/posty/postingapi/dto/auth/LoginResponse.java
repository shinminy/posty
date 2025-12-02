package com.posty.postingapi.dto.auth;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt
) {
}

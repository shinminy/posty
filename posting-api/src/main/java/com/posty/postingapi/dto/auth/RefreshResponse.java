package com.posty.postingapi.dto.auth;

import java.time.Instant;

public record RefreshResponse(
        String accessToken,
        Instant accessTokenExpiresAt
) {
}

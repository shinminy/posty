package com.posty.postingapi.infrastructure.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class RefreshTokenManager {

    private final RedisManager redisManager;

    public RefreshTokenManager(RedisManager redisManager) {
        this.redisManager = redisManager;
    }

    private String createRefreshTokenKey(String refreshToken) {
        return redisManager.createKey("refreshToken", refreshToken);
    }

    public void saveRefreshToken(String refreshToken, Long accountId, Duration ttl) {
        String redisKey = createRefreshTokenKey(refreshToken);
        redisManager.saveValueWithTtl(redisKey, accountId, ttl);
    }

    public Optional<Long> loadAccountIdByRefreshToken(String refreshToken) {
        String redisKey = createRefreshTokenKey(refreshToken);
        return Optional.ofNullable(redisManager.getValue(redisKey, Long.class));
    }

    public void clearRefreshToken(String refreshToken) {
        redisManager.delete(createRefreshTokenKey(refreshToken));
    }
}

package com.posty.postingapi.infrastructure.cache;

import com.posty.postingapi.properties.TimeToLiveProperties;
import com.posty.postingapi.properties.VerificationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class VerificationCacheManager {

    private final RedisManager redisManager;

    private final int maxCodeRequest;
    private final Duration rateLimit;
    private final Duration codeTtl;
    private final Duration verifiedTtl;

    public VerificationCacheManager(RedisManager redisManager, VerificationProperties verificationProperties, TimeToLiveProperties timeToLiveProperties) {
        this.redisManager = redisManager;

        maxCodeRequest = verificationProperties.getMaxCodeRequest();

        TimeToLiveProperties.VerificationProperties ttlProperties = timeToLiveProperties.getVerification();
        rateLimit = ttlProperties.getRateLimit();
        codeTtl = ttlProperties.getCode();
        verifiedTtl = ttlProperties.getVerified();
    }

    private String createVerificationCodeKey(VerificationChannel channel, String identifier) {
        return redisManager.createKey(channel.name().toLowerCase(), "verify", "code", identifier);
    }

    private String createEmailCodeKey(String email) {
        return createVerificationCodeKey(VerificationChannel.EMAIL, email);
    }

    private String createVerifiedKey(VerificationChannel channel, String identifier) {
        return redisManager.createKey(channel.name().toLowerCase(), "verify", "verified", identifier);
    }

    private String createEmailVerifiedKey(String email) {
        return createVerifiedKey(VerificationChannel.EMAIL, email);
    }

    private String createVerificationCountKey(VerificationChannel channel, String identifier) {
        return redisManager.createKey(channel.name().toLowerCase(), "verify", "count", identifier);
    }

    private String createEmailCountKey(String email) {
        return createVerificationCountKey(VerificationChannel.EMAIL, email);
    }

    private boolean tryConsumeVerificationQuota(String redisKey) {
        long count = redisManager.incrementWithTtlIfAbsent(redisKey, rateLimit);
        return count <= maxCodeRequest;
    }

    public boolean tryConsumeEmailVerificationQuota(String email) {
        String redisKey = createEmailCountKey(email);
        return tryConsumeVerificationQuota(redisKey);
    }

    public void saveEmailCode(String email, String verificationCode) {
        String redisKey = createEmailCodeKey(email);
        redisManager.saveValueWithTtl(redisKey, verificationCode, codeTtl);
    }

    public String getEmailCode(String email) {
        String redisKey = createEmailCodeKey(email);
        return redisManager.getValue(redisKey, String.class);
    }

    public void clearEmailCode(String email) {
        String redisKey = createEmailCodeKey(email);
        redisManager.delete(redisKey);
    }

    public void markEmailVerified(String email) {
        String redisKey = createEmailVerifiedKey(email);
        redisManager.saveValueWithTtl(redisKey, true, verifiedTtl);
    }
}

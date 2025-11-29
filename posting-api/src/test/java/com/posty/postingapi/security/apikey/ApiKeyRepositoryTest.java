package com.posty.postingapi.security.apikey;

import com.posty.postingapi.config.TimeConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ApiKeyRepositoryTest {

    private ApiKeyRepositoryImpl apiKeyRepositoryImpl;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TimeConfig timeConfig;

    @BeforeEach
    public void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2025-04-12T13:00:00Z"), ZoneId.of(timeConfig.getZoneId()));

        apiKeyRepositoryImpl = new ApiKeyRepositoryImpl(entityManager, fixedClock);
    }

    @Test
    public void testFindValidApiKey() {
        String keyHash = "hashedKey";

        boolean valid = apiKeyRepositoryImpl.isValid(keyHash);

        assertThat(valid).isFalse();
    }
}

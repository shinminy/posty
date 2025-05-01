package com.posty.postingapi.security.apikey;

import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.LocalDateTime;

@Repository
public class ApiKeyRepositoryImpl implements ApiKeyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final Clock clock;

    public ApiKeyRepositoryImpl(JPAQueryFactory queryFactory, Clock clock) {
        this.queryFactory = queryFactory;
        this.clock = clock;
    }

    @Autowired
    public ApiKeyRepositoryImpl(EntityManager entityManager, final Clock clock) {
        this.queryFactory = new JPAQueryFactory(entityManager);
        this.clock = clock;
    }

    @Override
    public boolean isValid(String keyHash) {
        if (StringUtils.isEmpty(keyHash)) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(clock);

        QApiKey qApiKey = QApiKey.apiKey;

        ApiKey apiKey = queryFactory
                .selectFrom(qApiKey)
                .where(
                        qApiKey.keyHash.eq(keyHash),
                        qApiKey.startsAt.loe(now),
                        qApiKey.expiresAt.goe(now)
                )
                .fetchOne();

        return apiKey != null;
    }
}

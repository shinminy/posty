package com.posty.postingapi.infrastructure.persistence.series;

import com.posty.postingapi.domain.series.SeriesRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class SeriesRepositoryImpl implements SeriesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SeriesRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
}

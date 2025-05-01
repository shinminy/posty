package com.posty.postingapi.domain.post;

import com.posty.postingapi.domain.account.QAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SeriesRepositoryImpl implements SeriesRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public SeriesRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }
}

package com.posty.postingapi.infrastructure.persistence;

import jakarta.annotation.PostConstruct;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.Objects;

public abstract class BaseQuerydslRepositorySupport extends QuerydslRepositorySupport {

    public BaseQuerydslRepositorySupport(Class<?> domainClass) {
        super(domainClass);
    }

    @PostConstruct
    private void init() {
        Objects.requireNonNull(getQuerydsl(), "Querydsl not initialized properly");
    }
}

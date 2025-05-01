package com.posty.postingapi.domain.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        QAccount qAccount = QAccount.account;

        Account account = queryFactory
                .selectFrom(qAccount)
                .where(qAccount.email.eq(email))
                .fetchOne();

        return Optional.ofNullable(account);
    }
}

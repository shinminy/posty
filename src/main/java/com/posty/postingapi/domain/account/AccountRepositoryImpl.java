package com.posty.postingapi.domain.account;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public boolean existsByEmail(String email) {
        QAccount qAccount = QAccount.account;

        return queryFactory
                .selectOne()
                .from(qAccount)
                .where(
                        qAccount.email.lower().eq(email.toLowerCase()),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetchFirst() != null;
    }

    @Override
    public boolean existsByName(String name) {
        QAccount qAccount = QAccount.account;

        return queryFactory
                .selectOne()
                .from(qAccount)
                .where(
                        qAccount.name.lower().eq(name.toLowerCase()),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetchFirst() != null;
    }

    @Override
    public Optional<Account> findByEmail(String email) {
        QAccount qAccount = QAccount.account;

        Account account = queryFactory
                .selectFrom(qAccount)
                .where(
                        qAccount.email.lower().eq(email.toLowerCase()),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetchOne();

        return Optional.ofNullable(account);
    }
}

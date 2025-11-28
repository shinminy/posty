package com.posty.postingapi.infrastructure.persistence.account;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepositoryCustom;
import com.posty.postingapi.domain.account.AccountStatus;
import com.posty.postingapi.domain.account.QAccount;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public AccountRepositoryImpl(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    @Override
    public boolean existsNonDeletedById(Long id) {
        QAccount qAccount = QAccount.account;

        return queryFactory
                .selectOne()
                .from(qAccount)
                .where(
                        qAccount.id.eq(id),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetchFirst() != null;
    }

    @Override
    public boolean existsNonDeletedByEmail(String email) {
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
    public boolean existsNonDeletedByName(String name) {
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
    public Optional<Account> findNonDeletedById(Long id) {
        QAccount qAccount = QAccount.account;

        Account account = queryFactory
                .selectFrom(qAccount)
                .where(
                        qAccount.id.eq(id),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetchOne();

        return Optional.ofNullable(account);
    }

    @Override
    public List<Account> findNonDeletedByIdIn(List<Long> ids) {
        QAccount qAccount = QAccount.account;

        return queryFactory
                .selectFrom(qAccount)
                .where(
                        qAccount.id.in(ids),
                        qAccount.status.ne(AccountStatus.DELETED)
                )
                .fetch();
    }

    @Override
    public Optional<Account> findNonDeletedByEmail(String email) {
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

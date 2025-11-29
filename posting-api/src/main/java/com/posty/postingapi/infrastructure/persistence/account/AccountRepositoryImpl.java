package com.posty.postingapi.infrastructure.persistence.account;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepositoryCustom;
import com.posty.postingapi.domain.account.AccountStatus;
import com.posty.postingapi.domain.account.QAccount;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryImpl extends BaseQuerydslRepositorySupport implements AccountRepositoryCustom {

    public AccountRepositoryImpl() {
        super(Account.class);
    }

    @Override
    public boolean existsNonDeletedById(Long id) {
        QAccount account = QAccount.account;

        return from(account)
                .where(
                        account.id.eq(id),
                        account.status.ne(AccountStatus.DELETED)
                )
                .select(account.id)
                .fetchFirst() != null;
    }

    @Override
    public boolean existsNonDeletedByEmail(String email) {
        QAccount account = QAccount.account;

        return from(account)
                .where(
                        account.email.lower().eq(email.toLowerCase()),
                        account.status.ne(AccountStatus.DELETED)
                )
                .select(account.id)
                .fetchFirst() != null;
    }

    @Override
    public boolean existsNonDeletedByName(String name) {
        QAccount account = QAccount.account;

        return from(account)
                .where(
                        account.name.lower().eq(name.toLowerCase()),
                        account.status.ne(AccountStatus.DELETED)
                )
                .select(account.id)
                .fetchFirst() != null;
    }

    @Override
    public Optional<Account> findNonDeletedById(Long id) {
        QAccount account = QAccount.account;

        return Optional.ofNullable(
                from(account)
                        .where(
                                account.id.eq(id),
                                account.status.ne(AccountStatus.DELETED)
                        )
                        .fetchOne()
        );
    }

    @Override
    public List<Account> findNonDeletedByIdIn(List<Long> ids) {
        QAccount account = QAccount.account;

        return from(account)
                .where(
                        account.id.in(ids),
                        account.status.ne(AccountStatus.DELETED)
                )
                .fetch();
    }

    @Override
    public Optional<Account> findNonDeletedByEmail(String email) {
        QAccount account = QAccount.account;

        return Optional.ofNullable(
                from(account)
                        .where(
                                account.email.lower().eq(email.toLowerCase()),
                                account.status.ne(AccountStatus.DELETED)
                        )
                        .fetchOne()
        );
    }
}

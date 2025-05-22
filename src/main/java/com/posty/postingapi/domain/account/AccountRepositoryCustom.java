package com.posty.postingapi.domain.account;

import java.util.Optional;

public interface AccountRepositoryCustom {
    boolean existsNonDeletedByEmail(String email);
    boolean existsNonDeletedByName(String name);
    Optional<Account> findNonDeletedById(Long id);
    Optional<Account> findNonDeletedByEmail(String email);
}

package com.posty.postingapi.domain.account;

import java.util.List;
import java.util.Optional;

public interface AccountRepositoryCustom {
    boolean existsNonDeletedByEmail(String email);
    boolean existsNonDeletedByName(String name);
    Optional<Account> findNonDeletedById(Long id);
    List<Account> findNonDeletedByIdIn(List<Long> ids);
    Optional<Account> findNonDeletedByEmail(String email);
}

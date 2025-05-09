package com.posty.postingapi.domain.account;

import java.util.Optional;

public interface AccountRepositoryCustom {
    boolean existsByEmail(String email);
    boolean existsByName(String name);
    Optional<Account> findByEmail(String email);
}

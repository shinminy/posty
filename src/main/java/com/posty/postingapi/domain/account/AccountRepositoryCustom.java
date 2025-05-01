package com.posty.postingapi.domain.account;

import java.util.Optional;

public interface AccountRepositoryCustom {
    Optional<Account> findByEmail(String email);
}

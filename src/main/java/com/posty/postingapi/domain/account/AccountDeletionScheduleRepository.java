package com.posty.postingapi.domain.account;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountDeletionScheduleRepository extends JpaRepository<AccountDeletionSchedule, Long>, AccountDeletionScheduleRepositoryCustom {

    Optional<AccountDeletionSchedule> findByAccountId(Long accountId);
}

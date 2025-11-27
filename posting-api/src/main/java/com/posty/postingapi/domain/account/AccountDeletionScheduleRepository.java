package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.common.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountDeletionScheduleRepository extends JpaRepository<AccountDeletionSchedule, Long>, AccountDeletionScheduleRepositoryCustom {

    Optional<AccountDeletionSchedule> findByAccountId(Long accountId);

    List<AccountDeletionSchedule> findAllByStatus(ScheduleStatus status);
}

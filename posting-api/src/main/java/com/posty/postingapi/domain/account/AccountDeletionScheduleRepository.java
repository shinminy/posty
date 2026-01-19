package com.posty.postingapi.domain.account;

import com.posty.postingapi.domain.common.ScheduleStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountDeletionScheduleRepository extends JpaRepository<AccountDeletionSchedule, Long>, AccountDeletionScheduleRepositoryCustom {

    Optional<AccountDeletionSchedule> findByAccountId(Long accountId);

    Slice<AccountDeletionSchedule> findAllByStatus(ScheduleStatus status, Pageable pageable);
}

package com.posty.postingapi.domain.account;

import java.time.LocalDateTime;
import java.util.List;

public interface AccountDeletionScheduleRepositoryCustom {

    List<AccountDeletionSchedule> findScheduledBefore(LocalDateTime cutoff);
}

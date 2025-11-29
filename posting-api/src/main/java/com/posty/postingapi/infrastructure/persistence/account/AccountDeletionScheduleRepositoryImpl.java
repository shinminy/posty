package com.posty.postingapi.infrastructure.persistence.account;

import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import com.posty.postingapi.domain.account.AccountDeletionScheduleRepositoryCustom;
import com.posty.postingapi.domain.account.QAccountDeletionSchedule;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AccountDeletionScheduleRepositoryImpl extends BaseQuerydslRepositorySupport implements AccountDeletionScheduleRepositoryCustom {

    public AccountDeletionScheduleRepositoryImpl() {
        super(AccountDeletionSchedule.class);
    }

    @Override
    public List<AccountDeletionSchedule> findScheduledBefore(LocalDateTime cutoff) {
        QAccountDeletionSchedule schedule = QAccountDeletionSchedule.accountDeletionSchedule;

        return from(schedule)
                .where(
                        schedule.status.eq(ScheduleStatus.SCHEDULED),
                        schedule.scheduledAt.before(cutoff)
                )
                .fetch();
    }
}

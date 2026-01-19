package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import com.posty.postingapi.domain.account.AccountDeletionScheduleRepository;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.domain.series.Series;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AccountDeletionService {


    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;

    private final Clock clock;

    public AccountDeletionService(
            AccountDeletionScheduleRepository accountDeletionScheduleRepository,
            Clock clock
    ) {
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;

        this.clock = clock;
    }

    @Transactional
    public List<Long> markAccountsDeletionInProgress() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<AccountDeletionSchedule> schedules = accountDeletionScheduleRepository.findScheduledBefore(now);

        schedules.forEach(AccountDeletionSchedule::markInProgress);

        return schedules.stream()
                .map(AccountDeletionSchedule::getId)
                .toList();
    }

    @Transactional
    public List<Long> processDeletionSchedules(int batchSize) {
        final LocalDateTime now = LocalDateTime.now(clock);

        Slice<AccountDeletionSchedule> schedules = accountDeletionScheduleRepository.findAllByStatus(
                ScheduleStatus.IN_PROGRESS,
                PageRequest.of(0, batchSize, Sort.by("id").ascending())
        );

        List<Long> accountIds = new ArrayList<>();
        for(AccountDeletionSchedule schedule : schedules) {
            try {
                Account account = schedule.getAccount();

                List<Series> copiedSeries = new ArrayList<>(account.getManagedSeries());
                copiedSeries.forEach(account::removeManagedSeries);

                account.markDeleted(now);
                schedule.markCompleted(account);

                accountIds.add(account.getId());
            } catch(Exception e) {
                log.error("Error processing accounts deletion schedule (ID: {})", schedule.getId(), e);
                schedule.markFailed();

            }
        }

        return accountIds;
    }
}

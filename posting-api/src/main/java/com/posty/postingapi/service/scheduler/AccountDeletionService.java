package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import com.posty.postingapi.domain.account.AccountDeletionScheduleRepository;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.domain.series.Series;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public void markAccountsDeletionInProgress() {
        LocalDateTime now = LocalDateTime.now(clock);

        List<AccountDeletionSchedule> schedules = accountDeletionScheduleRepository.findScheduledBefore(now);
        if (schedules.isEmpty()) {
            log.info("No accounts for deletion.");
            return;
        }

        schedules.forEach(AccountDeletionSchedule::markInProgress);

        log.info("{} account deletion schedules marked as IN_PROGRESS. IDs: {}", schedules.size(),
                schedules.stream().map(AccountDeletionSchedule::getId).map(String::valueOf).collect(Collectors.joining(",")));
    }

    @Transactional
    public void processAccountsDeletion() {
        LocalDateTime now = LocalDateTime.now(clock);

        List<AccountDeletionSchedule> schedules = accountDeletionScheduleRepository.findAllByStatus(ScheduleStatus.IN_PROGRESS);
        if (schedules.isEmpty()) {
            log.info("No accounts for deletion (IN_PROGRESS).");
            return;
        }

        List<Account> accounts = new ArrayList<>();
        schedules.forEach(schedule -> {
            Account account = schedule.getAccount();

            List<Series> copiedSeries = new ArrayList<>(account.getManagedSeries());
            copiedSeries.forEach(account::removeManagedSeries);

            account.markDeleted(now);
            schedule.markCompleted(account);

            accounts.add(account);
        });

        log.info("{} accounts soft-deleted. IDs: {}", accounts.size(),
                accounts.stream().map(Account::getId).map(String::valueOf).collect(Collectors.joining(",")));
    }
}

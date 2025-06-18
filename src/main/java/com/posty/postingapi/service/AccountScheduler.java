package com.posty.postingapi.service;

import com.posty.postingapi.config.SchedulerConfig;
import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.domain.series.Series;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountScheduler {

    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;
    private final AccountRepository accountRepository;

    private final Clock clock;

    public AccountScheduler(
            AccountDeletionScheduleRepository accountDeletionScheduleRepository, AccountRepository accountRepository,
            Clock clock, SchedulerConfig schedulerConfig
    ) {
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;
        this.accountRepository = accountRepository;

        this.clock = clock;
    }

    @Scheduled(cron = "${scheduler.account.deletion.cron}")
    @Transactional
    public void runAccountDeletionSchedules() {
        log.info("Account deletion scheduler started...");

        LocalDateTime now = LocalDateTime.now(clock);

        List<AccountDeletionSchedule> pendingSchedules = accountDeletionScheduleRepository.findScheduledBefore(now);

        if (pendingSchedules.isEmpty()) {
            log.info("No accounts scheduled for deletion.");
            return;
        }

        List<AccountDeletionSchedule> inProgressSchedules = pendingSchedules.stream()
                .map(schedule -> schedule.withStatus(ScheduleStatus.IN_PROGRESS))
                .toList();

        accountDeletionScheduleRepository.saveAll(inProgressSchedules);

        List<AccountDeletionSchedule> completedSchedules = inProgressSchedules.stream()
                .map(schedule -> {
                    Account account = schedule.getAccount();
                    List<Series> copiedSeries = new ArrayList<>(account.getManagedSeries());
                    copiedSeries.forEach(series -> series.removeManager(account));
                    return schedule.completedWith(account.deleted(now));
                })
                .toList();

        List<Account> accounts = completedSchedules.stream()
                .map(AccountDeletionSchedule::getAccount)
                .toList();

        accountRepository.saveAll(accounts);

        log.info("{} accounts soft-deleted. IDs: {}", accounts.size(),
                accounts.stream().map(Account::getId).map(String::valueOf).collect(Collectors.joining(",")));

        accountDeletionScheduleRepository.saveAll(completedSchedules);

        log.info("Account deletion scheduler ended!");
    }
}

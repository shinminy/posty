package com.posty.postingapi.service;

import com.posty.postingapi.config.SchedulerConfig;
import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.domain.common.ScheduleStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountScheduler {

    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;
    private final AccountRepository accountRepository;

    private final Clock clock;

    private final int deletionGracePeriodDays;

    public AccountScheduler(
            AccountDeletionScheduleRepository accountDeletionScheduleRepository, AccountRepository accountRepository,
            Clock clock, SchedulerConfig schedulerConfig
    ) {
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;
        this.accountRepository = accountRepository;

        this.clock = clock;

        this.deletionGracePeriodDays = schedulerConfig.getAccount().getDeletion().getGracePeriodDays();
    }

    @Scheduled(cron = "${scheduler.account.deletion.cron}")
    @Transactional
    public void runAccountDeletionSchedules() {
        log.info("Account deletion scheduler started...");

        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime cutoff = now.minusDays(deletionGracePeriodDays);

        log.info("now: {}, cutoff: {}", now, cutoff);

        List<AccountDeletionSchedule> pendingSchedules = accountDeletionScheduleRepository.findScheduledBefore(cutoff);

        if (pendingSchedules.isEmpty()) {
            log.info("No accounts scheduled for deletion.");
            return;
        }

        List<AccountDeletionSchedule> inProgressSchedules = pendingSchedules.stream()
                .map(schedule -> schedule.withStatus(ScheduleStatus.IN_PROGRESS))
                .collect(Collectors.toList());

        accountDeletionScheduleRepository.saveAll(inProgressSchedules);

        List<AccountDeletionSchedule> completedSchedules = inProgressSchedules.stream()
                .map(schedule -> schedule.completedWith(schedule.getAccount().deleted(now)))
                .collect(Collectors.toList());

        List<Account> accounts = completedSchedules.stream()
                .map(AccountDeletionSchedule::getAccount)
                .collect(Collectors.toList());

        accountRepository.saveAll(accounts);

        log.info("{} accounts soft-deleted. IDs: {}", accounts.size(),
                accounts.stream().map(Account::getId).map(String::valueOf).collect(Collectors.joining(",")));

        accountDeletionScheduleRepository.saveAll(completedSchedules);

        log.info("Account deletion scheduler ended!");
    }
}

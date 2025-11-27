package com.posty.postingapi.scheduler;

import com.posty.postingapi.service.scheduler.AccountDeletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AccountScheduler {

    private final AccountDeletionService accountDeletionService;

    public AccountScheduler(AccountDeletionService accountDeletionService) {
        this.accountDeletionService = accountDeletionService;
    }

    @Scheduled(cron = "${scheduler.account.deletion.cron}")
    public void runAccountDeletionSchedules() {
        log.info("Account deletion scheduler started...");
        accountDeletionService.markAccountsDeletionInProgress();
        accountDeletionService.processAccountsDeletion();
        log.info("Account deletion scheduler ended!");
    }
}

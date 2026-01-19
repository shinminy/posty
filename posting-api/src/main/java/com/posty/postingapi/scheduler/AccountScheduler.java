package com.posty.postingapi.scheduler;

import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.service.scheduler.AccountDeletionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AccountScheduler {

    private final AccountDeletionService accountDeletionService;

    private final int batchSize;

    public AccountScheduler(
            AccountDeletionService accountDeletionService,
            SchedulerProperties schedulerProperties
    ) {
        this.accountDeletionService = accountDeletionService;

        batchSize = schedulerProperties.getAccount().getDeletion().getBatchSize();
    }

    @Scheduled(cron = "${scheduler.account.deletion.cron}")
    public void runAccountDeletionSchedules() {
        List<Long> scheduleIds = accountDeletionService.markAccountsDeletionInProgress();
        if (scheduleIds.isEmpty()) {
            log.info("No accounts for deletion.");
            return;
        }

        log.info("{} account deletion schedules marked as IN_PROGRESS. IDs: {}", scheduleIds.size(), scheduleIds);

        List<Long> accountIds;
        while (!(accountIds = accountDeletionService.processDeletionSchedules(batchSize)).isEmpty()) {
            log.info("{} accounts soft-deleted. IDs: {}", accountIds.size(), accountIds);
        }
    }
}

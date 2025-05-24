package com.posty.postingapi.error;

import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateAccountDeletionException extends RuntimeException {

    public DuplicateAccountDeletionException(final AccountDeletionSchedule schedule) {
        super("This account already has a scheduled deletion (" + schedule.getStatus() + ", updated at " + schedule.getUpdatedAt() + ").");
    }
}

package com.posty.postingapi.service.scheduler;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import com.posty.postingapi.domain.account.AccountDeletionScheduleRepository;
import com.posty.postingapi.domain.common.ScheduleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountDeletionServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    private final LocalDateTime now = LocalDateTime.now(clock);

    @Mock
    private AccountDeletionScheduleRepository repository;

    private AccountDeletionService accountDeletionService;

    @BeforeEach
    void setUp() {
        accountDeletionService = new AccountDeletionService(repository, clock);
    }

    @Test
    @DisplayName("계정 삭제 스케줄 처리 - 성공한 계정은 완료 처리하고 실패한 계정은 실패 마킹")
    void processDeletionSchedules_SuccessAndFailure() {
        // given
        Account account1 = mock(Account.class);
        Account account2 = mock(Account.class);

        AccountDeletionSchedule successSchedule = spy(AccountDeletionSchedule.create(account1, now));
        AccountDeletionSchedule failSchedule = spy(AccountDeletionSchedule.create(account2, now));

        doThrow(new RuntimeException("DB Error")).when(account2).markDeleted(any());

        Slice<AccountDeletionSchedule> slice = new SliceImpl<>(List.of(successSchedule, failSchedule));
        when(repository.findAllByStatus(eq(ScheduleStatus.IN_PROGRESS), any())).thenReturn(slice);

        // when
        List<Long> resultIds = accountDeletionService.processDeletionSchedules(10);

        // then
        assertThat(resultIds).hasSize(1);
        verify(successSchedule).markCompleted(any());
        verify(failSchedule).markFailed();
    }
}

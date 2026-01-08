package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import com.posty.postingapi.error.AccountUpdateNotAllowedException;
import com.posty.postingapi.error.DuplicateAccountDeletionException;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.support.TestTimeConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountDeletionScheduleRepository accountDeletionScheduleRepository;

    @Mock
    private WriterCacheManager writerCacheManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SchedulerProperties schedulerProperties;

    private Clock clock;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        var accountConfig = mock(SchedulerProperties.AccountSchedulerProperties.class);
        var deletionConfig = mock(SchedulerProperties.AccountSchedulerProperties.AccountDeletionProperties.class);
        given(schedulerProperties.getAccount()).willReturn(accountConfig);
        given(accountConfig.getDeletion()).willReturn(deletionConfig);
        given(deletionConfig.getGracePeriodDays()).willReturn(1);

        clock = new TestTimeConfig().testClock();

        accountService = new AccountService(
                accountRepository, accountDeletionScheduleRepository,
                writerCacheManager, clock, passwordEncoder, schedulerProperties
        );
    }

    @Test
    @DisplayName("계정 상세 조회 성공")
    void getAccountDetail_Success() {
        // given
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("tester")
                .status(AccountStatus.NORMAL)
                .build();
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));

        // when
        AccountDetailResponse response = accountService.getAccountDetail(accountId);

        // then
        assertThat(response.getEmail()).isEqualTo(account.getEmail());
        assertThat(response.getName()).isEqualTo(account.getName());
    }

    @Test
    @DisplayName("계정 상세 조회 실패 - 존재하지 않는 계정")
    void getAccountDetail_NotFound() {
        // given
        Long accountId = 1L;
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountService.getAccountDetail(accountId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("계정 생성 성공")
    void createAccount_Success() {
        // given
        AccountCreateRequest request = new AccountCreateRequest(
                "new@example.com", "password", "newName", "+82-10-1234-5678"
        );

        request.normalize();

        given(accountRepository.existsNonDeletedByEmail(request.getEmail())).willReturn(false);
        given(accountRepository.existsNonDeletedByName(request.getName())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("hashedPassword");
        
        Account savedAccount = Account.builder()
                .id(1L)
                .email(request.getEmail())
                .name(request.getName())
                .build();
        given(accountRepository.save(any(Account.class))).willReturn(savedAccount);

        // when
        AccountDetailResponse response = accountService.createAccount(request);

        // then
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("계정 생성 실패 - 중복된 이메일")
    void createAccount_DuplicateEmail() {
        // given
        AccountCreateRequest request = new AccountCreateRequest(
                "duplicate@example.com", "password", "newName", "+82-10-1234-5678"
        );
        given(accountRepository.existsNonDeletedByEmail(request.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(DuplicateAccountException.class);
    }

    @Test
    @DisplayName("계정 수정 성공")
    void updateAccount_Success() {
        // given
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .email("test@example.com")
                .name("oldName")
                .status(AccountStatus.NORMAL)
                .build();
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));

        AccountUpdateRequest request = new AccountUpdateRequest(
                "newPassword", "newName", "+82-10-8765-4321"
        );

        given(accountRepository.existsNonDeletedByName("newname")).willReturn(false);

        // when
        accountService.updateAccount(accountId, request);

        // then
        assertThat(account.getName()).isEqualTo("newname");
        verify(writerCacheManager).clearAccountName(accountId);
    }

    @Test
    @DisplayName("계정 수정 실패 - 삭제 대기 중인 계정")
    void updateAccount_Fail_WaitingForDeletion() {
        // given
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .status(AccountStatus.WAITING_FOR_DELETION)
                .build();
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));

        AccountUpdateRequest request = new AccountUpdateRequest("newPassword", "newName", null);

        // when & then
        assertThatThrownBy(() -> accountService.updateAccount(accountId, request))
                .isInstanceOf(AccountUpdateNotAllowedException.class);
    }

    @Test
    @DisplayName("계정 수정 실패 - 중복된 이름")
    void updateAccount_Fail_DuplicateName() {
        // given
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .name("oldName")
                .status(AccountStatus.NORMAL)
                .build();
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));
        given(accountRepository.existsNonDeletedByName("newname")).willReturn(true);

        AccountUpdateRequest request = new AccountUpdateRequest(null, "newName", null);

        // when & then
        assertThatThrownBy(() -> accountService.updateAccount(accountId, request))
                .isInstanceOf(DuplicateAccountException.class);
    }

    @Test
    @DisplayName("계정 삭제 스케줄링 성공")
    void scheduleAccountDeletion_Success() {
        // given
        Long accountId = 1L;
        Account account = Account.builder()
                .id(accountId)
                .status(AccountStatus.NORMAL)
                .build();
        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));
        given(accountDeletionScheduleRepository.findByAccountId(accountId)).willReturn(Optional.empty());
        given(accountDeletionScheduleRepository.save(any(AccountDeletionSchedule.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountDeleteResponse response = accountService.scheduleAccountDeletion(accountId);

        // then
        assertThat(account.getStatus()).isEqualTo(AccountStatus.WAITING_FOR_DELETION);
        assertThat(response.getScheduledAt()).isEqualTo(LocalDateTime.now(clock).plusDays(1));
        verify(accountDeletionScheduleRepository).save(any(AccountDeletionSchedule.class));
        verify(accountRepository).save(account);
    }

    @Test
    @DisplayName("계정 삭제 스케줄링 실패 - 이미 스케줄링됨")
    void scheduleAccountDeletion_Fail_AlreadyScheduled() {
        // given
        Long accountId = 1L;
        Account account = Account.builder().id(accountId).build();
        AccountDeletionSchedule existingSchedule = AccountDeletionSchedule.builder()
                .account(account)
                .status(ScheduleStatus.SCHEDULED)
                .build();

        given(accountRepository.findNonDeletedById(accountId)).willReturn(Optional.of(account));
        given(accountDeletionScheduleRepository.findByAccountId(accountId)).willReturn(Optional.of(existingSchedule));

        // when & then
        assertThatThrownBy(() -> accountService.scheduleAccountDeletion(accountId))
                .isInstanceOf(DuplicateAccountDeletionException.class);
    }
}

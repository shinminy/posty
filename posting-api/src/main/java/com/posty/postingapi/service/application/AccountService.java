package com.posty.postingapi.service.application;

import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.error.AccountUpdateNotAllowedException;
import com.posty.postingapi.error.DuplicateAccountDeletionException;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;

    private final WriterCacheManager writerCacheManager;

    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    private final int deletionGracePeriodDays;

    public AccountService(
            AccountRepository accountRepository, AccountDeletionScheduleRepository accountDeletionScheduleRepository,
            WriterCacheManager writerCacheManager,
            Clock clock, PasswordEncoder passwordEncoder, SchedulerProperties schedulerProperties
    ) {
        this.accountRepository = accountRepository;
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;

        this.writerCacheManager = writerCacheManager;

        this.clock = clock;
        this.passwordEncoder = passwordEncoder;

        this.deletionGracePeriodDays = schedulerProperties.getAccount().getDeletion().getGracePeriodDays();
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    }

    public AccountDetailResponse getAccountDetail(Long accountId) {
        Account account = findAccountById(accountId);

        return AccountMapper.toAccountDetailResponse(account);
    }

    public AccountDetailResponse createAccount(AccountCreateRequest request) {
        request.normalize();

        String email = request.getEmail();
        if (accountRepository.existsNonDeletedByEmail(email)) {
            throw new DuplicateAccountException(email);
        }

        String name = request.getName();
        if (accountRepository.existsNonDeletedByName(name)) {
            throw new DuplicateAccountException(name);
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        Account account = AccountMapper.toEntity(request, hashedPassword);
        Account saved = accountRepository.save(account);

        return AccountMapper.toAccountDetailResponse(saved);
    }

    public void updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = findAccountById(accountId);

        if (account.getStatus() == AccountStatus.WAITING_FOR_DELETION) {
            throw new AccountUpdateNotAllowedException("This account is scheduled for deletion.");
        }

        request.normalize();

        String oldName = account.getName();
        String newName = request.getName();
        boolean hasNewName = StringUtils.hasText(newName);
        if (hasNewName && !newName.equalsIgnoreCase(oldName) && accountRepository.existsNonDeletedByName(newName)) {
            throw new DuplicateAccountException(newName);
        }

        String newPassword = request.getPassword();
        String hashedPassword = StringUtils.hasText(newPassword) ? passwordEncoder.encode(newPassword) : null;

        account.updateProfile(request, hashedPassword);
        accountRepository.save(account);

        if (hasNewName && !newName.equals(oldName)) {
            writerCacheManager.clearAccountName(accountId);
        }
    }

    public AccountDeleteResponse scheduleAccountDeletion(Long accountId) {
        Account account = findAccountById(accountId);

        Optional<AccountDeletionSchedule> optionalSchedule = accountDeletionScheduleRepository.findByAccountId(account.getId());
        if (optionalSchedule
                .filter(schedule -> !schedule.getStatus().canReschedule())
                .isPresent()) {
            throw new DuplicateAccountDeletionException(optionalSchedule.get());
        }
        optionalSchedule.ifPresent(accountDeletionScheduleRepository::delete);

        LocalDateTime now = LocalDateTime.now(clock);
        AccountDeletionSchedule schedule = AccountDeletionSchedule.builder()
                .account(account)
                .status(ScheduleStatus.SCHEDULED)
                .scheduledAt(now.plusDays(deletionGracePeriodDays))
                .createdAt(now)
                .updatedAt(now)
                .build();
        AccountDeletionSchedule saved = accountDeletionScheduleRepository.save(schedule);

        account.markWaitingForDeletion();
        accountRepository.save(account);

        return AccountMapper.toAccountDeleteResponse(saved);
    }
}

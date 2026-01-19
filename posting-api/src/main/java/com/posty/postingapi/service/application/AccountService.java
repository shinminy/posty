package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.event.AccountChangedEvent;
import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import com.posty.postingapi.properties.SchedulerProperties;
import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.error.AccountUpdateNotAllowedException;
import com.posty.postingapi.error.DuplicateAccountDeletionException;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    private final int deletionGracePeriodDays;

    public AccountService(
            AccountRepository accountRepository,
            AccountDeletionScheduleRepository accountDeletionScheduleRepository,
            ApplicationEventPublisher applicationEventPublisher,
            Clock clock,
            PasswordEncoder passwordEncoder,
            SchedulerProperties schedulerProperties
    ) {
        this.accountRepository = accountRepository;
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;

        this.applicationEventPublisher = applicationEventPublisher;

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

    @Transactional
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

    @Transactional
    public void updateAccount(Long accountId, AccountUpdateRequest request) {
        Account account = findAccountById(accountId);

        if (account.getStatus() == AccountStatus.WAITING_FOR_DELETION) {
            throw new AccountUpdateNotAllowedException("This account is scheduled for deletion.");
        }

        request.normalize();

        String oldName = account.getName();
        String newName = request.getName();
        boolean nameChanged = isNameChanged(oldName, newName);
        if (nameChanged && accountRepository.existsNonDeletedByName(newName)) {
            throw new DuplicateAccountException(newName);
        }

        String rawPassword = request.getPassword();
        String hashedPassword = StringUtils.hasText(rawPassword) ? passwordEncoder.encode(rawPassword) : null;

        account.updateProfile(request, hashedPassword);
        accountRepository.save(account);

        if (nameChanged && !newName.equals(oldName)) {
            applicationEventPublisher.publishEvent(new AccountChangedEvent(accountId));
        }
    }

    private boolean isNameChanged(String oldName, String newName) {
        return StringUtils.hasText(newName) && !newName.equals(oldName);
    }

    @Transactional
    public AccountDeleteResponse scheduleAccountDeletion(Long accountId) {
        Account account = findAccountById(accountId);

        accountDeletionScheduleRepository.findByAccountId(account.getId())
                .ifPresent(schedule -> {
                    if (!schedule.getStatus().canReschedule()) {
                        throw new DuplicateAccountDeletionException(schedule);
                    }

                    accountDeletionScheduleRepository.delete(schedule);
                });

        LocalDateTime scheduledAt = LocalDateTime.now(clock).plusDays(deletionGracePeriodDays);
        AccountDeletionSchedule schedule = AccountDeletionSchedule.create(account, scheduledAt);
        AccountDeletionSchedule saved = accountDeletionScheduleRepository.save(schedule);

        account.markWaitingForDeletion();
        accountRepository.save(account);

        return AccountMapper.toAccountDeleteResponse(saved);
    }
}

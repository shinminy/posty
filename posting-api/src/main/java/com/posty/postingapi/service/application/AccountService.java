package com.posty.postingapi.service.application;

import com.posty.postingapi.properties.SchedulerConfig;
import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.domain.common.ScheduleStatus;
import com.posty.postingapi.dto.*;
import com.posty.postingapi.dto.SeriesSummary;
import com.posty.postingapi.error.AccountUpdateNotAllowedException;
import com.posty.postingapi.error.DuplicateAccountDeletionException;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import com.posty.postingapi.mapper.SeriesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDeletionScheduleRepository accountDeletionScheduleRepository;

    private final Clock clock;
    private final PasswordEncoder passwordEncoder;

    private final int deletionGracePeriodDays;

    public AccountService(
            AccountRepository accountRepository, AccountDeletionScheduleRepository accountDeletionScheduleRepository,
            Clock clock, PasswordEncoder passwordEncoder, SchedulerConfig schedulerConfig
    ) {
        this.accountRepository = accountRepository;
        this.accountDeletionScheduleRepository = accountDeletionScheduleRepository;

        this.clock = clock;
        this.passwordEncoder = passwordEncoder;

        this.deletionGracePeriodDays = schedulerConfig.getAccount().getDeletion().getGracePeriodDays();
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
        Account oldAccount = findAccountById(accountId);

        if (oldAccount.getStatus() == AccountStatus.WAITING_FOR_DELETION) {
            throw new AccountUpdateNotAllowedException("This account is scheduled for deletion.");
        }

        request.normalize();

        String newName = request.getName();
        if (StringUtils.hasText(newName) && !oldAccount.getName().equalsIgnoreCase(newName) && accountRepository.existsNonDeletedByName(newName)) {
            throw new DuplicateAccountException(newName);
        }

        String newPassword = request.getPassword();
        String hashedPassword = StringUtils.hasText(newPassword) ? passwordEncoder.encode(newPassword) : null;

        Account newAccount = oldAccount.updatedBy(request, hashedPassword);
        accountRepository.save(newAccount);
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

        Account accountToBeUpdated = account.waitingForDeleting();
        accountRepository.save(accountToBeUpdated);

        return AccountMapper.toAccountDeleteResponse(saved);
    }

    public List<SeriesSummary> getManagedSeriesList(Long accountId) {
        Account account = findAccountById(accountId);

        return account.getManagedSeries().stream()
                .map(SeriesMapper::toSeriesSummary)
                .collect(Collectors.toList());
    }
}

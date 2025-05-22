package com.posty.postingapi.service;

import com.posty.postingapi.domain.account.*;
import com.posty.postingapi.dto.AccountCreateRequest;
import com.posty.postingapi.dto.AccountDetailResponse;
import com.posty.postingapi.dto.AccountUpdateRequest;
import com.posty.postingapi.dto.SeriesSummary;
import com.posty.postingapi.error.AccountUpdateNotAllowedException;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import com.posty.postingapi.mapper.SeriesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;

        this.passwordEncoder = passwordEncoder;
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

        Account newAccount = oldAccount.withUpdatedFields(request, hashedPassword);
        accountRepository.save(newAccount);
    }

    public List<SeriesSummary> getManagedSeriesList(Long accountId) {
        Account account = findAccountById(accountId);

        return account.getManagedSeries().stream()
                .map(SeriesMapper::toSeriesSummary)
                .collect(Collectors.toList());
    }
}

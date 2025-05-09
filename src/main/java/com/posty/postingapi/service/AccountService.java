package com.posty.postingapi.service;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.post.SeriesRepository;
import com.posty.postingapi.dto.AccountCreateRequest;
import com.posty.postingapi.dto.AccountDetailResponse;
import com.posty.postingapi.dto.SeriesSummary;
import com.posty.postingapi.error.DuplicateAccountException;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import com.posty.postingapi.mapper.SeriesMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final SeriesRepository seriesRepository;

    public AccountService(AccountRepository accountRepository, SeriesRepository seriesRepository) {
        this.accountRepository = accountRepository;
        this.seriesRepository = seriesRepository;
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with id = " + accountId));
    }

    public AccountDetailResponse getAccountDetail(Long accountId) {
        Account account = findAccountById(accountId);

        return AccountMapper.toAccountDetailResponse(account);
    }

    public AccountDetailResponse createAccount(AccountCreateRequest request) {

        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateAccountException("Email already exists.");
        }

        if (accountRepository.existsByName(request.getName())) {
            throw new DuplicateAccountException("Name already exists.");
        }

        Account account = AccountMapper.toEntity(request);
        Account saved = accountRepository.save(account);

        return AccountMapper.toAccountDetailResponse(saved);
    }

    public List<SeriesSummary> getManagedSeriesList(Long accountId) {
        Account account = findAccountById(accountId);

        return account.getManagedSeries().stream()
                .map(SeriesMapper::toSeriesSummary)
                .collect(Collectors.toList());
    }
}

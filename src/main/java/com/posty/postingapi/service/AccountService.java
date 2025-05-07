package com.posty.postingapi.service;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.post.SeriesRepository;
import com.posty.postingapi.dto.AccountDetailResponse;
import com.posty.postingapi.dto.SeriesSummary;
import com.posty.postingapi.error.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        return new AccountDetailResponse(account);
    }

    public List<SeriesSummary> getManagedSeriesList(Long accountId) {
        Account account = findAccountById(accountId);

        return account.getManagedSeries().stream()
                .map(SeriesSummary::new)
                .collect(Collectors.toList());
    }
}

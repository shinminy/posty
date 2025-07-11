package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountDeletionSchedule;
import com.posty.postingapi.domain.account.AccountStatus;
import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountSummary;

public class AccountMapper {

    public static AccountDetailResponse toAccountDetailResponse(Account entity) {
        return new AccountDetailResponse(
                entity.getId(),
                entity.getEmail(),
                entity.getName(),
                entity.getMobileNumber(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getLastLoginAt(),
                entity.getLockedAt()
        );
    }

    public static AccountSummary toAccountSummary(Account entity) {
        return new AccountSummary(
                entity.getId(),
                entity.getName()
        );
    }

    public static Account toEntity(AccountCreateRequest request, String hashedPassword) {
        return Account.builder()
                .email(request.getEmail())
                .password(hashedPassword)
                .name(request.getName())
                .mobileNumber(request.getMobileNumber())
                .status(AccountStatus.NORMAL)
                .build();
    }

    public static AccountDeleteResponse toAccountDeleteResponse(AccountDeletionSchedule entity) {
        return new AccountDeleteResponse(
                entity.getScheduledAt()
        );
    }
}

package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountStatus;
import com.posty.postingapi.dto.AccountCreateRequest;
import com.posty.postingapi.dto.AccountDetailResponse;
import com.posty.postingapi.dto.AccountSummary;
import org.apache.commons.codec.digest.DigestUtils;

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

    public static Account toEntity(AccountCreateRequest request) {
        String hashedPassword = DigestUtils.sha512Hex(request.getPassword());

        return new Account(
                request.getEmail(),
                hashedPassword,
                request.getName(),
                request.getMobileNumber(),
                AccountStatus.NORMAL
        );
    }
}

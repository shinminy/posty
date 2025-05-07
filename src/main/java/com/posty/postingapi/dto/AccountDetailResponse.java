package com.posty.postingapi.dto;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AccountDetailResponse {

    private Long id;

    private String email;

    private String name;

    private String mobileNumber;

    private AccountStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @Schema(description = "status가 LOCKED일 때만 값 존재, 이외에는 null")
    private LocalDateTime lockedAt;

    public AccountDetailResponse(Long id, String email, String name, String mobileNumber, AccountStatus status, LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime lastLoginAt, LocalDateTime lockedAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
        this.lockedAt = lockedAt;
    }

    public AccountDetailResponse(Account account) {
        this(
                account.getId(),
                account.getEmail(),
                account.getName(),
                account.getMobileNumber(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                account.getLastLoginAt(),
                account.getLockedAt()
        );
    }
}

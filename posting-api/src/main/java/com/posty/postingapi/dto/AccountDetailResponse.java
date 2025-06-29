package com.posty.postingapi.dto;

import com.posty.postingapi.domain.account.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
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
}

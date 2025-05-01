package com.posty.postingapi.controller;

import com.posty.postingapi.dto.AccountDetail;
import com.posty.postingapi.dto.SimpleSeries;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "계정 관리 API", description = "계정 관련 CRUD API")
@CommonErrorResponses
@Validated
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "계정 상세정보 조회", description = "계정 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountDetail.class)))
    @GetMapping("/{account-id}")
    public AccountDetail getAccount(@PathVariable("account-id") Long accountId) {
        return accountService.getAccountDetail(accountId);
    }

    @Operation(summary = "관리하는 시리즈 조회", description = "해당 계정이 관리 중인 시리즈 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = SimpleSeries.class))))
    @GetMapping("/{account-id}/managed-series")
    public List<SimpleSeries> getManagedSeriesList(@PathVariable("account-id") Long accountId) {
        return accountService.getManagedSeriesList(accountId);
    }
}

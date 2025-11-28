package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.account.AccountCreateRequest;
import com.posty.postingapi.dto.account.AccountDeleteResponse;
import com.posty.postingapi.dto.account.AccountDetailResponse;
import com.posty.postingapi.dto.account.AccountUpdateRequest;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "계정 관리 API", description = "계정 관련 CRUD API")
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "계정 상세정보 조회", description = "계정 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountDetailResponse.class)))
    @GetMapping("/{accountId}")
    public AccountDetailResponse getAccount(@PathVariable Long accountId) {
        return accountService.getAccountDetail(accountId);
    }

    @Operation(summary = "계정 생성", description = "계정을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = AccountDetailResponse.class)))
    @PostMapping
    public ResponseEntity<AccountDetailResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        AccountDetailResponse body = accountService.createAccount(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "계정 수정", description = "계정 정보를 수정합니다. (관리 중인 시리즈 정보는 제외)")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PutMapping("/{accountId}")
    public ResponseEntity<Void> updateAccount(@PathVariable Long accountId, @Valid @RequestBody AccountUpdateRequest request) {
        accountService.updateAccount(accountId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "계정 삭제", description = "계정 삭제를 요청합니다. 삭제 중 상태로 변경되며, 설정된 유예 기간 후 삭제됩니다. (실제 삭제시간은 삭제 예정일 다음 날 새벽)")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountDeleteResponse.class)))
    @DeleteMapping("/{accountId}")
    public AccountDeleteResponse deleteAccount(@PathVariable Long accountId) {
        return accountService.scheduleAccountDeletion(accountId);
    }
}

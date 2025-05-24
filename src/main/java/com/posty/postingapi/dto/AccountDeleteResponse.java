package com.posty.postingapi.dto;

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
public class AccountDeleteResponse {

    @Schema(description = "삭제 예정일 (실제 삭제시간은 예정일 다음 날 새벽)")
    LocalDateTime scheduledAt;
}

package com.posty.postingapi.dto.post;

import com.posty.postingapi.dto.account.AccountSummary;
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
public class PostBlockResponse {

    private Long id;

    @Schema(description = "블록 순서 (번호)")
    private Integer order;

    private AccountSummary writer;

    @Schema(oneOf = { TextContentResponse.class, MediaContentResponse.class })
    private ContentResponse content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

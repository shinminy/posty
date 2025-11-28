package com.posty.postingapi.dto.comment;

import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.post.PostSummary;
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
public class CommentDetailResponse {

    private Long id;

    @Schema(description = "해당 댓글이 달린 포스트")
    private PostSummary post;

    private String content;

    @Schema(description = "댓글 작성자")
    private AccountSummary writer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

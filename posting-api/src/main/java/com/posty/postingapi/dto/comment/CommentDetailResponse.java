package com.posty.postingapi.dto.comment;

import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.post.PostSummary;
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

    private PostSummary post;

    private String content;

    private AccountSummary writer;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

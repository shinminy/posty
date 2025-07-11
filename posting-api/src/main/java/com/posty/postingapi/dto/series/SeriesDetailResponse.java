package com.posty.postingapi.dto.series;

import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.dto.account.AccountSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class SeriesDetailResponse {

    private Long id;

    private String title;

    private String description;

    @Schema(description = "해당 시리즈 관리자 목록")
    private List<AccountSummary> managers;

    @Schema(description = "해당 시리즈 내 포스트 작성에 참여한 작성자 목록")
    private List<String> writers;

    private List<PostSummary> posts;
}

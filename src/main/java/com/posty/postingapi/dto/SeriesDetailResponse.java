package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Series;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

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

    public SeriesDetailResponse(Long id, String title, String description, List<AccountSummary> managers, List<String> writers, List<PostSummary> posts) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.managers = managers;
        this.writers = writers;
        this.posts = posts;
    }

    public SeriesDetailResponse(Series series, List<String> writers, List<PostSummary> posts) {
        this(
                series.getId(),
                series.getTitle(),
                series.getDescription(),
                series.getManagers().stream()
                        .map(AccountSummary::new)
                        .collect(Collectors.toList()),
                writers,
                posts
        );
    }
}

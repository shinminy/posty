package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.dto.series.SeriesCreateRequest;
import com.posty.postingapi.dto.series.SeriesDetailResponse;
import com.posty.postingapi.dto.series.SeriesSummary;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public class SeriesMapper {

    public static SeriesDetailResponse toSeriesDetailResponse(Series entity, List<String> writers, Page<PostSummary> posts) {
        List<AccountSummary> managers = entity.getManagers().stream()
                .map(AccountMapper::toAccountSummary)
                .toList();

        return new SeriesDetailResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                managers,
                writers,
                posts
        );
    }

    public static SeriesSummary toSeriesSummary(Series entity) {
        return new SeriesSummary(
                entity.getId(),
                entity.getTitle()
        );
    }

    public static Series toEntity(SeriesCreateRequest request, Set<Account> managers) {
        return Series.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .managers(managers)
                .build();
    }
}

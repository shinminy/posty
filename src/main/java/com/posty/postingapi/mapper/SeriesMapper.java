package com.posty.postingapi.mapper;

import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.dto.AccountSummary;
import com.posty.postingapi.dto.PostSummary;
import com.posty.postingapi.dto.SeriesDetailResponse;
import com.posty.postingapi.dto.SeriesSummary;

import java.util.List;
import java.util.stream.Collectors;

public class SeriesMapper {

    public static SeriesDetailResponse toSeriesDetailResponse(Series entity, List<String> writers, List<PostSummary> posts) {
        List<AccountSummary> managers = entity.getManagers().stream()
                .map(AccountMapper::toAccountSummary)
                .collect(Collectors.toList());

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
}

package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Series;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class SeriesSummary {

    private Long id;

    private String title;

    public SeriesSummary(Series series) {
        this(series.getId(), series.getTitle());
    }
}

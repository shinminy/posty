package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Series;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SeriesSummary {

    private Long id;

    private String title;

    public SeriesSummary(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public SeriesSummary(Series series) {
        this(series.getId(), series.getTitle());
    }
}

package com.posty.postingapi.dto;

import com.posty.postingapi.domain.post.Series;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SimpleSeries {

    private Long id;

    private String title;

    public SimpleSeries(Long id, String title) {
        this.id = id;
        this.title = title;
    }

    public SimpleSeries(Series series) {
        this(series.getId(), series.getTitle());
    }
}

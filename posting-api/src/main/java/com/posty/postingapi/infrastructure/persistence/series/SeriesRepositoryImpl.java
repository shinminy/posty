package com.posty.postingapi.infrastructure.persistence.series;

import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepositoryCustom;
import com.posty.postingapi.infrastructure.persistence.BaseQuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

@Repository
public class SeriesRepositoryImpl extends BaseQuerydslRepositorySupport implements SeriesRepositoryCustom {

    public SeriesRepositoryImpl() {
        super(Series.class);
    }
}

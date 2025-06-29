package com.posty.postingapi.domain.series;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long>, SeriesRepositoryCustom {
}

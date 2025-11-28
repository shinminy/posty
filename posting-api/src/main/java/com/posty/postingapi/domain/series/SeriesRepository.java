package com.posty.postingapi.domain.series;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long>, SeriesRepositoryCustom {

    Page<Series> findByManagersId(Long accountId, Pageable pageable);
}

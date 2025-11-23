package com.posty.postingapi.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostBlockRepositoryCustom {
    List<Long> findDistinctWriterIdsBySeriesId(Long seriesId);
    List<Long> findDistinctWriterIdsByPostId(Long postId);

    Page<PostBlock> findPageByPostIdOrderByOrderNo(Long postId, Pageable pageable);
}

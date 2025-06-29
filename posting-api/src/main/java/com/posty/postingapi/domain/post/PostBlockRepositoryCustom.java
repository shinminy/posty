package com.posty.postingapi.domain.post;

import java.util.List;

public interface PostBlockRepositoryCustom {
    List<String> findDistinctWriterNamesBySeriesId(Long seriesId);
    List<String> findDistinctWriterNamesByPostId(Long postId);
}

package com.posty.postingapi.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<Post> findAllByWriterId(Long writerId, Pageable pageable);
}

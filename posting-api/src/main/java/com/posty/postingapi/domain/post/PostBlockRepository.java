package com.posty.postingapi.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBlockRepository extends JpaRepository<PostBlock, Long>, PostBlockRepositoryCustom {

    Page<PostBlock> findAllByPostId(Long postId, Pageable pageable);
}

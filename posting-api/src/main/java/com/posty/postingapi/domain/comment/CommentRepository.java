package com.posty.postingapi.domain.comment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {

    Page<Comment> findAllByPostId(Long postId, Pageable pageable);

    @Transactional
    long deleteAllByPostId(Long postId);
}

package com.posty.postingapi.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PostBlockRepository extends JpaRepository<PostBlock, Long>, PostBlockRepositoryCustom {
}

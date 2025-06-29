package com.posty.postingapi.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaRepository extends JpaRepository<Media, Long>, MediaRepositoryCustom {
}

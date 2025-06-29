package com.posty.postingapi.security.apikey;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long>, ApiKeyRepositoryCustom {
}

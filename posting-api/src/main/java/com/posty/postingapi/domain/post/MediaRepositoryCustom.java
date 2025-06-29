package com.posty.postingapi.domain.post;

import java.util.List;

public interface MediaRepositoryCustom {
    List<Media> findFailedMediaForRetry(int maxRetryCount);
}

package com.posty.postingapi.security.apikey;

public interface ApiKeyRepositoryCustom {
    boolean isValid(String keyHash);
}

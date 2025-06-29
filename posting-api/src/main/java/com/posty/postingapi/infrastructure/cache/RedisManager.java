package com.posty.postingapi.infrastructure.cache;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisManager {

    private static final String EMPTY_PLACEHOLDER = "__EMPTY__";

    protected final RedisTemplate<String, String> redisTemplate;

    public RedisManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveList(@NotEmpty String key, @NotNull List<String> values) {
        if (values.isEmpty()) {
            redisTemplate.opsForList().rightPush(key, EMPTY_PLACEHOLDER);
        } else {
            redisTemplate.opsForList().rightPushAll(key, values);
        }
    }

    public List<String> getList(@NotEmpty String key) {
        List<String> list = redisTemplate.opsForList().range(key, 0, -1);
        if (list != null && list.size() == 1 && EMPTY_PLACEHOLDER.equals(list.get(0))) {
            return List.of();
        }
        return list;
    }

    public void updateList(@NotEmpty String key, @NotNull List<String> values) {
        redisTemplate.delete(key);

        if (values.isEmpty()) {
            redisTemplate.opsForList().rightPush(key, EMPTY_PLACEHOLDER);
        } else {
            redisTemplate.opsForList().rightPushAll(key, values);
        }
    }

    public void delete(@NotEmpty String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(@NotEmpty String key) {
        return redisTemplate.hasKey(key);
    }

    public String createKey(@NotEmpty String ... keys) {
        return String.join(":", keys);
    }
}

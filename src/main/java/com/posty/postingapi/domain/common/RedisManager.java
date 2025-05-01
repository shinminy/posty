package com.posty.postingapi.domain.common;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisManager {

    protected final RedisTemplate<String, String> redisTemplate;

    public RedisManager(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveList(String key, List<String> values) {
        redisTemplate.opsForList().rightPushAll(key, values);
    }

    public List<String> getList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public void updateList(String key, List<String> values) {
        redisTemplate.opsForList().rightPushAll(key, values);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public String createKey(String ... keys) {
        return String.join(":", keys);
    }
}

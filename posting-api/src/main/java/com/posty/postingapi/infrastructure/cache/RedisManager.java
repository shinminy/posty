package com.posty.postingapi.infrastructure.cache;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Validated
@Component
public class RedisManager {

    private static final String EMPTY_PLACEHOLDER = "__EMPTY__";

    protected final RedisTemplate<String, Object> redisTemplate;

    public RedisManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> void saveValue(@NotBlank String key, @NotNull T value) {
        saveValueWithTtl(key, value, null);
    }

    // ttl이 null/0/음수면 TTL(유효기간) 없이 저장
    public <T> void saveValueWithTtl(@NotBlank String key, @NotNull T value, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            redisTemplate.opsForValue().set(key, value);
            return;
        }

        redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    public <T> T getValue(@NotBlank String key, @NotNull Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        return value == null ? null : convertToType(value, clazz);
    }

    public void saveList(@NotBlank String key, @NotNull List<?> values) {
        if (values.isEmpty()) {
            redisTemplate.opsForList().rightPush(key, EMPTY_PLACEHOLDER);
        } else {
            @SuppressWarnings("unchecked")
            Collection<Object> collection = (Collection<Object>) values;
            redisTemplate.opsForList().rightPushAll(key, collection);
        }
    }

    public <T> List<T> getList(@NotBlank String key, Class<T> clazz) {
        List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
        return list == null || list.isEmpty() || (list.size() == 1 && EMPTY_PLACEHOLDER.equals(list.get(0)))
                ? List.of()
                : list.stream()
                    .map(object -> convertToType(object, clazz))
                    .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> T convertToType(Object obj, Class<T> clazz) {
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }

        if (clazz == Long.class && obj instanceof Number) {
            return (T) Long.valueOf(((Number) obj).longValue());
        }

        if (clazz == Integer.class && obj instanceof Number) {
            return (T) Integer.valueOf(((Number) obj).intValue());
        }

        if (clazz == String.class) {
            return (T) obj.toString();
        }

        return clazz.cast(obj);
    }

    public void updateList(@NotBlank String key, @NotNull List<?> values) {
        redisTemplate.delete(key);
        saveList(key, values);
    }

    public Map<String, Object> getValuesAsMap(@NotEmpty List<String> keys) {
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        if (values == null || values.isEmpty()) {
            return Map.of();
        }

        Map<String, Object> result = new HashMap<>();
        int size = Math.min(keys.size(), values.size());
        for (int i = 0; i < size; i++) {
            Object value = values.get(i);
            if (value != null) {
                result.put(keys.get(i), value);
            }
        }
        return result;
    }

    public void saveValues(@NotEmpty Map<String, Object> keyValueMap) {
        saveValuesWithTtl(keyValueMap, null);
    }

    // ttl이 null/0/음수면 TTL(유효기간) 없이 저장
    public void saveValuesWithTtl(@NotEmpty Map<String, Object> keyValueMap, Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            redisTemplate.opsForValue().multiSet(keyValueMap);
            return;
        }

        keyValueMap.forEach((key, value) ->
                redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS)
        );
    }

    public void delete(@NotBlank String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(@NotBlank String key) {
        return redisTemplate.hasKey(key);
    }

    public String createKey(String prefix, String... suffixes) {
        if (suffixes.length == 0) {
            return prefix;
        }
        return prefix + ":" + String.join(":", suffixes);
    }
}

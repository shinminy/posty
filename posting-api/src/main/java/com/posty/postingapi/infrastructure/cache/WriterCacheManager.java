package com.posty.postingapi.infrastructure.cache;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.properties.TimeToLiveConfig;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class WriterCacheManager {

    private static final String WRITER_IDS_KEY = "writerIds";
    private static final String NAME_KEY = "name";

    private final RedisManager redisManager;
    private final AccountRepository accountRepository;
    private final PostBlockRepository postBlockRepository;

    private final Duration accountNameTtl;

    public WriterCacheManager(RedisManager redisManager,
                              AccountRepository accountRepository,
                              PostBlockRepository postBlockRepository,
                              TimeToLiveConfig timeToLiveConfig) {
        this.redisManager = redisManager;
        this.accountRepository = accountRepository;
        this.postBlockRepository = postBlockRepository;

        this.accountNameTtl = timeToLiveConfig.getAccountNameCache();
    }

    private String createSeriesWriterIdsKey(long seriesId) {
        return redisManager.createKey("series", String.valueOf(seriesId), WRITER_IDS_KEY);
    }

    private String createPostWriterIdsKey(long postId) {
        return redisManager.createKey("post", String.valueOf(postId), WRITER_IDS_KEY);
    }

    private String createAccountNameKey(long accountId) {
        return redisManager.createKey("account", String.valueOf(accountId), NAME_KEY);
    }

    private List<String> loadAccountNames(List<Long> accountIds) {
        Map<Long, String> idToKey = accountIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::createAccountNameKey
                ));

        Map<String, Object> cachedNameMap = redisManager.getValuesAsMap(new ArrayList<>(idToKey.values()));

        List<String> names = new ArrayList<>(accountIds.size());

        List<Long> missingIds = new ArrayList<>();

        for (Long accountId : accountIds) {
            String key = idToKey.get(accountId);
            Object value = cachedNameMap.get(key);

            if (value instanceof String name) {
                names.add(name);
            } else {
                missingIds.add(accountId);
            }
        }

        if (!missingIds.isEmpty()) {
            List<Account> accounts = accountRepository.findAllById(missingIds);

            Map<String, Object> missingNameMap = new HashMap<>();

            for (Account account : accounts) {
                String name = account.getName();

                names.add(name);
                missingNameMap.put(createAccountNameKey(account.getId()), name);
            }

            if (!missingNameMap.isEmpty()) {
                redisManager.saveValuesWithTtl(missingNameMap, accountNameTtl);
            }
        }

        Collections.sort(names);

        return names;
    }

    public List<String> loadWritersOfSeries(long seriesId) {
        List<Long> writerIds = loadWriterIdsOfSeries(seriesId);
        return loadAccountNames(writerIds);
    }

    private List<Long> loadWriterIdsOfSeries(long seriesId) {
        String redisKey = createSeriesWriterIdsKey(seriesId);

        List<Long> writerIds;
        if (redisManager.hasKey(redisKey)) {
            writerIds = redisManager.getList(redisKey, Long.class);
        } else {
            writerIds = postBlockRepository.findDistinctWriterIdsBySeriesId(seriesId);
            redisManager.saveList(redisKey, writerIds);
        }

        return writerIds;
    }

    public List<String> loadWritersOfPosts(long postId) {
        List<Long> writerIds = loadWriterIdsOfPosts(postId);
        return loadAccountNames(writerIds);
    }

    private List<Long> loadWriterIdsOfPosts(long postId) {
        String redisKey = createPostWriterIdsKey(postId);

        List<Long> writerIds;
        if (redisManager.hasKey(redisKey)) {
            writerIds = redisManager.getList(redisKey, Long.class);
        } else {
            writerIds = postBlockRepository.findDistinctWriterIdsByPostId(postId);
            redisManager.saveList(redisKey, writerIds);
        }

        return writerIds;
    }

    public void clearWritersOfSeries(long seriesId) {
        redisManager.delete(createSeriesWriterIdsKey(seriesId));
    }

    public void clearWritersOfPosts(long postId, long seriesId) {
        redisManager.delete(createPostWriterIdsKey(postId));
        clearWritersOfSeries(seriesId);
    }

    public void clearAccountName(long accountId) {
        redisManager.delete(createAccountNameKey(accountId));
    }
}

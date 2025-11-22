package com.posty.postingapi.infrastructure.cache;

import com.posty.postingapi.domain.post.PostBlockRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WriterCacheManager {

    private static final String WRITER_KEY = "writers";

    private final RedisManager redisManager;
    private final PostBlockRepository postBlockRepository;

    public WriterCacheManager(RedisManager redisManager, PostBlockRepository postBlockRepository) {
        this.redisManager = redisManager;
        this.postBlockRepository = postBlockRepository;
    }

    public List<String> loadWritersOfSeries(long seriesId) {
        String redisKey = redisManager.createKey("series", String.valueOf(seriesId), WRITER_KEY);

        List<String> writers;
        if (redisManager.hasKey(redisKey)) {
            writers = redisManager.getList(redisKey);
        } else {
            writers = postBlockRepository.findDistinctWriterNamesBySeriesId(seriesId);
            redisManager.saveList(redisKey, writers);
        }

        return writers;
    }

    public List<String> loadWritersOfPosts(long postId) {
        String redisKey = redisManager.createKey("post", String.valueOf(postId), WRITER_KEY);

        List<String> writers;
        if (redisManager.hasKey(redisKey)) {
            writers = redisManager.getList(redisKey);
        } else {
            writers = postBlockRepository.findDistinctWriterNamesByPostId(postId);
            redisManager.saveList(redisKey, writers);
        }

        return writers;
    }

    public void clearWritersOfSeries(long seriesId) {
        String redisKey = redisManager.createKey("series", String.valueOf(seriesId), WRITER_KEY);
        redisManager.delete(redisKey);
    }

    public void clearWritersOfPosts(long postId, long seriesId) {
        String redisKey = redisManager.createKey("post", String.valueOf(postId), WRITER_KEY);
        redisManager.delete(redisKey);

        clearWritersOfSeries(seriesId);
    }
}

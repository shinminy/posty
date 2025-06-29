package com.posty.postingapi.domain.common;

import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.infrastructure.cache.RedisManager;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WriterSearch {

    private static final String WRITER_KEY = "writers";

    private final RedisManager redisManager;
    private final PostBlockRepository postBlockRepository;

    public WriterSearch(RedisManager redisManager, PostBlockRepository postBlockRepository) {
        this.redisManager = redisManager;
        this.postBlockRepository = postBlockRepository;
    }

    public List<String> searchWritersOfSeries(long seriesId) {
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

    public List<String> searchWritersOfPosts(long postId) {
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
}

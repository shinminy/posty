package com.posty.postingapi.service;

import com.posty.postingapi.domain.common.WriterSearch;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.dto.SeriesDetail;
import com.posty.postingapi.dto.SimplePost;
import com.posty.postingapi.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final PostRepository postRepository;

    private final WriterSearch writerSearch;

    public SeriesService(SeriesRepository seriesRepository, PostRepository postRepository, WriterSearch writerSearch) {
        this.seriesRepository = seriesRepository;
        this.postRepository = postRepository;

        this.writerSearch = writerSearch;
    }

    private Series findSeriesById(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found with id = " + seriesId));
    }

    public SeriesDetail getSeriesDetail(Long seriesId, int page, int size) {
        Series series = findSeriesById(seriesId);

        List<String> writers = writerSearch.searchWritersOfSeries(seriesId);

        PageRequest pageable = PageRequest.of(page-1, size);
        Page<Post> posts = postRepository.findPostsBySeriesId(seriesId, pageable);

        List<SimplePost> postData = posts.stream()
                .map(SimplePost::new)
                .collect(Collectors.toList());

        return new SeriesDetail(series, writers, postData);
    }
}

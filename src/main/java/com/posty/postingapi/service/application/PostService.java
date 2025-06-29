package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.common.WriterSearch;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.dto.*;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.PostBlockMapper;
import com.posty.postingapi.mapper.PostMapper;
import com.posty.postingapi.mq.MediaEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostBlockRepository postBlockRepository;
    private final SeriesRepository seriesRepository;
    private final AccountRepository accountRepository;

    private final WriterSearch writerSearch;

    private final MediaEventPublisher mediaEventPublisher;

    public PostService(
            PostRepository postRepository,
            PostBlockRepository postBlockRepository,
            SeriesRepository seriesRepository,
            AccountRepository accountRepository,
            WriterSearch writerSearch,
            MediaEventPublisher mediaEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postBlockRepository = postBlockRepository;
        this.seriesRepository = seriesRepository;
        this.accountRepository = accountRepository;

        this.writerSearch = writerSearch;

        this.mediaEventPublisher = mediaEventPublisher;
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    public PostDetailResponse getPostDetail(Long postId, int page, int size) {
        Post post = findPostById(postId);

        List<String> writers = writerSearch.searchWritersOfPosts(postId);

        PageRequest pageable = PageRequest.of(page-1, size);
        Page<PostBlock> blockData = postBlockRepository.findAllByPostId(postId, pageable);

        List<PostBlockResponse> blocks = blockData.stream()
                .map(PostBlockMapper::toPostBlockResponse)
                .collect(Collectors.toList());

        return PostMapper.toPostDetailResponse(post, writers, blocks);
    }

    public PostDetailResponse createPost(PostCreateRequest request) {
        Long seriesId = request.getSeriesId();
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));

        request.normalize();

        Post post = PostMapper.toEntity(request, series);
        List<PostBlock> postBlocks = request.getBlocks().stream()
                .map(blockRequest -> {
                    Long writerId = blockRequest.getWriterId();
                    Account writer = accountRepository.findNonDeletedById(writerId)
                            .orElseThrow(() -> new ResourceNotFoundException("Account", writerId));

                    return PostBlockMapper.toEntity(blockRequest, post, writer);
                })
                .toList();
        post.getBlocks().addAll(postBlocks);

        Post saved = postRepository.save(post);

        saved.getBlocks().forEach(block -> {
            if (block.getContentType() != ContentType.TEXT) {
                mediaEventPublisher.publishMediaUpload(block.getMedia().getId());
            }
        });

        List<String> writers = writerSearch.searchWritersOfPosts(saved.getId());
        List<PostBlockResponse> blocks = saved.getBlocks().stream().map(PostBlockMapper::toPostBlockResponse).toList();

        return PostMapper.toPostDetailResponse(saved, writers, blocks);
    }
}

package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.dto.post.*;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.PostBlockMapper;
import com.posty.postingapi.mapper.PostMapper;
import com.posty.postingapi.mq.MediaEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostBlockRepository postBlockRepository;
    private final SeriesRepository seriesRepository;
    private final AccountRepository accountRepository;

    private final WriterCacheManager writerCacheManager;

    private final MediaService mediaService;
    private final MediaEventPublisher mediaEventPublisher;

    public PostService(
            PostRepository postRepository,
            PostBlockRepository postBlockRepository,
            SeriesRepository seriesRepository,
            AccountRepository accountRepository,
            WriterCacheManager writerCacheManager,
            MediaService mediaService,
            MediaEventPublisher mediaEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postBlockRepository = postBlockRepository;
        this.seriesRepository = seriesRepository;
        this.accountRepository = accountRepository;

        this.writerCacheManager = writerCacheManager;

        this.mediaService = mediaService;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    public PostDetailResponse getPostDetail(Long postId, int page, int size) {
        Post post = findPostById(postId);

        List<String> writers = writerCacheManager.loadWritersOfPosts(postId);

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

        List<String> writers = new ArrayList<>();
        createPostBlocks(post, request.getBlocks(), writers);

        Post saved = postRepository.save(post);

        saved.getBlocks().stream()
                .filter(block -> block.getContentType() == ContentType.MEDIA)
                .map(PostBlock::getMedia)
                .forEach(mediaEventPublisher::publishMediaUpload);

        List<String> sortedWriters = writers.stream().distinct().sorted().toList();
        List<PostBlockResponse> blocks = saved.getBlocks().stream().map(PostBlockMapper::toPostBlockResponse).toList();

        return PostMapper.toPostDetailResponse(saved, sortedWriters, blocks);
    }

    private void createPostBlocks(Post post, List<PostBlockCreateRequest> blockRequests) {
        createPostBlocks(post, blockRequests, null);
    }

    private void createPostBlocks(Post post, List<PostBlockCreateRequest> blockRequests, List<String> writers) {
        if (blockRequests == null || blockRequests.isEmpty()) {
            return;
        }

        for (PostBlockCreateRequest blockRequest : blockRequests) {
            Long writerId = blockRequest.getWriterId();
            Account writer = accountRepository.findNonDeletedById(writerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account", writerId));

            PostBlock newBlock = PostBlockMapper.toEntity(blockRequest, post, writer);
            post.addBlock(newBlock);

            if (writers != null) {
                writers.add(writer.getName());
            }
        }
    }

    public void updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPostById(postId);

        request.normalize();

        Map<Long, Media> oldMediaMap = post.getBlocks().stream()
                .filter(block -> block.getContentType() == ContentType.MEDIA)
                .map(PostBlock::getMedia)
                .collect(Collectors.toMap(Media::getId, Function.identity()));

        post.updateTitle(request.getTitle());

        createPostBlocks(post, request.getNewBlocks());
        updatePostBlocks(post, request.getUpdatedBlocks());
        deletePostBlocks(request.getDeletedBlockIds());

        Post saved = postRepository.save(post);

        writerCacheManager.clearWritersOfPosts(postId, post.getSeries().getId());

        Map<Long, Media> newMediaMap = saved.getBlocks().stream()
                .filter(block -> block.getContentType() == ContentType.MEDIA)
                .map(PostBlock::getMedia)
                .collect(Collectors.toMap(Media::getId, Function.identity()));

        processMedia(oldMediaMap, newMediaMap);
    }

    private void updatePostBlocks(Post post, List<PostBlockUpdateRequest> blockRequests) {
        if (blockRequests == null || blockRequests.isEmpty()) {
            return;
        }

        Map<Long, PostBlock> blockMap = post.getBlocks().stream()
                .collect(Collectors.toMap(PostBlock::getId, Function.identity()));

        for (PostBlockUpdateRequest blockRequest : blockRequests) {
            Long blockId = blockRequest.getId();
            PostBlock block = blockMap.get(blockId);
            if (block == null) {
                throw new ResourceNotFoundException("PostBlock", blockId);
            }

            Long newWriterId = blockRequest.getWriterId();
            Account writer = newWriterId.equals(block.getWriter().getId())
                    ? block.getWriter()
                    : accountRepository.findNonDeletedById(newWriterId)
                            .orElseThrow(() -> new ResourceNotFoundException("Account", newWriterId));

            PostBlock temp = PostBlockMapper.toEntity(blockRequest, post, writer);

            block.updateMeta(temp.getOrderNo(), writer);
            if (!block.hasSameContent(temp)) {
                applyContentChange(block, temp);
            }
        }
    }

    private void applyContentChange(PostBlock target, PostBlock source) {
        if (source.getContentType() == ContentType.TEXT) {
            target.updateContentAsText(source.getTextContent());
        } else {
            target.updateContentAsMedia(source.getMedia());
        }
    }

    private void deletePostBlocks(List<Long> postBlockIds) {
        if (postBlockIds == null || postBlockIds.isEmpty()) {
            return;
        }

        postBlockRepository.deleteAllByIdInBatch(postBlockIds);
    }

    private void processMedia(Map<Long, Media> oldMedia, Map<Long, Media> newMedia) {
        Set<Long> oldMediaIds = oldMedia.keySet();
        Set<Long> newMediaIds = newMedia.keySet();

        newMedia.values().stream()
                .filter(media -> !oldMediaIds.contains(media.getId()))
                .forEach(mediaEventPublisher::publishMediaUpload);

        List<Media> deletedMediaList = oldMedia.values().stream()
                .filter(media -> !newMediaIds.contains(media.getId()))
                .toList();
        requestToDeleteMediaList(deletedMediaList);
    }

    private void requestToDeleteMediaList(List<Media> mediaList) {
        List<Media> waitingMediaList = mediaService.deleteOrPrepareMediaForDeletion(mediaList);
        waitingMediaList.forEach(mediaEventPublisher::publishMediaDelete);
    }

    public void deletePost(Long postId) {
        Post post = findPostById(postId);

        List<Media> mediaList = mediaService.findMediaByPostId(postId);

        postRepository.delete(post);

        writerCacheManager.clearWritersOfPosts(postId, post.getSeries().getId());

        requestToDeleteMediaList(mediaList);
    }
}

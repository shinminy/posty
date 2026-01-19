package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.event.MediaChangedEvent;
import com.posty.postingapi.domain.post.event.PostChangedEvent;
import com.posty.postingapi.domain.series.event.SeriesChangedEvent;
import com.posty.postingapi.dto.post.*;
import com.posty.postingapi.infrastructure.cache.WriterCacheManager;
import com.posty.postingapi.domain.post.*;
import com.posty.postingapi.domain.series.Series;
import com.posty.postingapi.domain.series.SeriesRepository;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.PostBlockMapper;
import com.posty.postingapi.mapper.PostMapper;
import com.posty.postingapi.properties.PaginationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostBlockRepository postBlockRepository;
    private final SeriesRepository seriesRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final WriterCacheManager writerCacheManager;

    private final MediaService mediaService;

    private final int defaultPage;
    private final int defaultPageSize;

    public PostService(
            PostRepository postRepository,
            PostBlockRepository postBlockRepository,
            SeriesRepository seriesRepository,
            AccountRepository accountRepository,
            CommentRepository commentRepository,
            ApplicationEventPublisher applicationEventPublisher,
            WriterCacheManager writerCacheManager,
            MediaService mediaService,
            PaginationProperties paginationProperties
    ) {
        this.postRepository = postRepository;
        this.postBlockRepository = postBlockRepository;
        this.seriesRepository = seriesRepository;
        this.accountRepository = accountRepository;
        this.commentRepository = commentRepository;

        this.applicationEventPublisher = applicationEventPublisher;

        this.writerCacheManager = writerCacheManager;

        this.mediaService = mediaService;

        defaultPage = paginationProperties.getDefaultPage();
        defaultPageSize = paginationProperties.getDefaultSize();
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    private Series findSeriesById(Long seriesId) {
        return seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series", seriesId));
    }

    private Account findWriterById(Long accountId) {
        return accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Writer", accountId));
    }

    private void createPostBlocks(Post post, List<PostBlockCreateRequest> blockRequests, List<String> writers) {
        if (blockRequests == null || blockRequests.isEmpty()) {
            return;
        }

        for (PostBlockCreateRequest blockRequest : blockRequests) {
            Long writerId = blockRequest.getWriterId();
            Account writer = findWriterById(writerId);

            PostBlock newBlock = PostBlockMapper.toEntity(blockRequest, post, writer);
            post.addBlock(newBlock);

            if (writers != null) {
                writers.add(writer.getName());
            }
        }
    }

    private List<Media> extractMediaFromBlocks(List<PostBlock> postBlockList) {
        return postBlockList.stream()
                .filter(block -> block.getContentType() == ContentType.MEDIA)
                .map(PostBlock::getMedia)
                .toList();
    }

    private void publishMediaCreatedEvents(List<Media> mediaList) {
        mediaList.stream()
                .map(media -> new MediaChangedEvent(
                        media.getId(),
                        MediaChangedEvent.MediaChangeType.CREATED
                ))
                .forEach(applicationEventPublisher::publishEvent);
    }

    private void publishMediaDeletedEvents(List<Media> mediaList) {
        mediaList.stream()
                .map(media -> new MediaChangedEvent(
                        media.getId(),
                        MediaChangedEvent.MediaChangeType.DELETED
                ))
                .forEach(applicationEventPublisher::publishEvent);
    }

    private void deleteMediaAndPublishEvents(List<Media> mediaList) {
        List<Media> waitingMediaList = mediaService.deleteOrPrepareMediaForDeletion(mediaList);
        publishMediaDeletedEvents(waitingMediaList);
    }

    public PostDetailResponse getPostDetail(Long postId, int page, int size) {
        Post post = findPostById(postId);

        List<String> writers = writerCacheManager.loadWritersOfPosts(postId);

        PageRequest pageable = PageRequest.of(page, size, PostBlock.SORT);
        Page<PostBlockResponse> blocks = postBlockRepository.findAllByPostId(postId, pageable)
                .map(PostBlockMapper::toPostBlockResponse);

        return PostMapper.toPostDetailResponse(post, writers, blocks);
    }

    @Transactional
    public PostDetailResponse createPost(PostCreateRequest request) {
        Long seriesId = request.getSeriesId();
        Series series = findSeriesById(seriesId);
        request.normalize();

        Post post = PostMapper.toEntity(request, series);
        List<String> writers = new ArrayList<>();
        createPostBlocks(post, request.getBlocks(), writers);

        Post saved = postRepository.save(post);

        List<PostBlock> blocks = saved.getBlocks();
        List<Media> mediaList = extractMediaFromBlocks(blocks);

        applicationEventPublisher.publishEvent(new SeriesChangedEvent(seriesId));
        publishMediaCreatedEvents(mediaList);

        return PostMapper.toPostDetailResponse(
                saved,
                sortAndDistinctWriters(writers),
                getInitialBlockPage(blocks)
        );
    }

    private List<String> sortAndDistinctWriters(List<String> writers) {
        return writers.stream()
                .distinct()
                .sorted()
                .toList();
    }

    private Page<PostBlockResponse> getInitialBlockPage(List<PostBlock> postBlockList) {
        List<PostBlockResponse> blockResponseList = postBlockList.stream()
                .sorted(Comparator.comparing(PostBlock::getOrderNo))
                .limit(defaultPageSize)
                .map(PostBlockMapper::toPostBlockResponse)
                .toList();

        return new PageImpl<>(
                blockResponseList,
                PageRequest.of(defaultPage, defaultPageSize, PostBlock.SORT),
                postBlockList.size()
        );
    }

    @Transactional
    public void updatePost(Long postId, PostUpdateRequest request) {
        Post post = findPostById(postId);
        request.normalize();

        List<Media> oldMediaList = extractMediaFromBlocks(post.getBlocks());

        post.updateTitle(request.getTitle());

        createPostBlocks(post, request.getNewBlocks());
        updatePostBlocks(post, request.getUpdatedBlocks());
        deletePostBlocks(post, request.getDeletedBlockIds());

        postRepository.save(post);

        List<Media> newMediaList = extractMediaFromBlocks(post.getBlocks());

        applicationEventPublisher.publishEvent(new PostChangedEvent(postId, post.getSeries().getId()));
        handleMediaChanges(oldMediaList, newMediaList);
    }

    private void createPostBlocks(Post post, List<PostBlockCreateRequest> blockRequests) {
        createPostBlocks(post, blockRequests, null);
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
                    : findWriterById(newWriterId);

            PostBlock temp = PostBlockMapper.toEntity(blockRequest, post, writer);

            block.updateMeta(temp.getOrderNo(), writer);
            if (!block.hasSameContent(temp)) {
                if (temp.getContentType() == ContentType.TEXT) {
                    block.updateContentAsText(temp.getTextContent());
                } else {
                    block.updateContentAsMedia(temp.getMedia());
                }
            }
        }
    }

    private void deletePostBlocks(Post post, List<Long> postBlockIds) {
        if (postBlockIds == null || postBlockIds.isEmpty()) {
            return;
        }

        post.removeBlocks(postBlockIds);
    }

    private void handleMediaChanges(List<Media> oldMedia, List<Media> newMedia) {
        Set<Long> newMediaIds = newMedia.stream().map(Media::getId).collect(Collectors.toSet());
        Set<Long> oldMediaIds = oldMedia.stream().map(Media::getId).collect(Collectors.toSet());

        List<Media> created = newMedia.stream()
                .filter(newItem -> !oldMediaIds.contains(newItem.getId()))
                .toList();

        List<Media> deleted = oldMedia.stream()
                .filter(oldItem -> !newMediaIds.contains(oldItem.getId()))
                .toList();

        publishMediaCreatedEvents(created);
        deleteMediaAndPublishEvents(deleted);
    }

    @Transactional
    public void deletePost(Long postId) {
        Post post = findPostById(postId);

        List<Media> mediaList = mediaService.findMediaByPostId(postId);

        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);

        applicationEventPublisher.publishEvent(new PostChangedEvent(postId, post.getSeries().getId()));
        deleteMediaAndPublishEvents(mediaList);
    }

    public Page<PostSummary> getPostsByWriter(Long accountId, Pageable pageable) {
        if (!accountRepository.existsNonDeletedById(accountId)) {
            throw new ResourceNotFoundException("Account", accountId);
        }

        Page<Post> posts = postRepository.findAllByWriterId(accountId, pageable);
        return posts.map(PostMapper::toPostSummary);
    }
}

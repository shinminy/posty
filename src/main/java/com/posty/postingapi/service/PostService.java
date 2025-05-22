package com.posty.postingapi.service;

import com.posty.postingapi.domain.common.WriterSearch;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.dto.PostBlockResponse;
import com.posty.postingapi.dto.PostDetailResponse;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.PostBlockMapper;
import com.posty.postingapi.mapper.PostMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostBlockRepository postBlockRepository;

    private final WriterSearch writerSearch;

    public PostService(PostRepository postRepository, PostBlockRepository postBlockRepository, WriterSearch writerSearch) {
        this.postRepository = postRepository;
        this.postBlockRepository = postBlockRepository;

        this.writerSearch = writerSearch;
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id = " + postId));
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
}

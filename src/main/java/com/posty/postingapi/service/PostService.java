package com.posty.postingapi.service;

import com.posty.postingapi.domain.common.WriterSearch;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.post.PostBlock;
import com.posty.postingapi.domain.post.PostBlockRepository;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.dto.PostBlockDetail;
import com.posty.postingapi.dto.PostDetail;
import com.posty.postingapi.error.ResourceNotFoundException;
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

    public PostDetail getPostDetail(Long postId, int page, int size) {
        Post post = findPostById(postId);

        List<String> writers = writerSearch.searchWritersOfPosts(postId);

        PageRequest pageable = PageRequest.of(page-1, size);
        Page<PostBlock> blocks = postBlockRepository.findBlocksByPostId(postId, pageable);

        List<PostBlockDetail> blockData = blocks.stream()
                .map(PostBlockDetail::new)
                .collect(Collectors.toList());

        return new PostDetail(post, writers, blockData);
    }
}

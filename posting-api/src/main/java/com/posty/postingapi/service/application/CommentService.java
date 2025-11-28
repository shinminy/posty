package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.account.Account;
import com.posty.postingapi.domain.account.AccountRepository;
import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.domain.post.Post;
import com.posty.postingapi.domain.post.PostRepository;
import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.comment.CommentUpdateRequest;
import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import com.posty.postingapi.mapper.CommentMapper;
import com.posty.postingapi.mapper.PostMapper;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;

    public CommentService(
            CommentRepository commentRepository,
            PostRepository postRepository,
            AccountRepository accountRepository
    ) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.accountRepository = accountRepository;
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
    }

    public CommentDetailResponse getCommentDetail(Long commentId) {
        Comment comment = findCommentById(commentId);

        PostSummary post = PostMapper.toPostSummary(comment.getPost());
        AccountSummary writer = AccountMapper.toAccountSummary(comment.getWriter());

        return CommentMapper.toCommentDetailResponse(comment, post, writer);
    }

    public CommentDetailResponse createComment(CommentCreateRequest request) {
        request.normalize();

        Long postId = request.getPostId();
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));

        Long accountId = request.getWriterId();
        Account writer = accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));

        Comment comment = CommentMapper.toEntity(request, post, writer);
        commentRepository.save(comment);

        return CommentMapper.toCommentDetailResponse(
                comment,
                PostMapper.toPostSummary(post),
                AccountMapper.toAccountSummary(writer)
        );
    }

    public void updateComment(Long commentId, CommentUpdateRequest request) {
        Comment comment = findCommentById(commentId);

        request.normalize();

        comment.updateContent(request.getContent());
        commentRepository.save(comment);
    }
}

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findNonDeletedById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", accountId));
    }

    public CommentDetailResponse getCommentDetail(Long commentId) {
        Comment comment = findCommentById(commentId);

        PostSummary post = PostMapper.toPostSummary(comment.getPost());
        AccountSummary writer = AccountMapper.toAccountSummary(comment.getWriter());

        return CommentMapper.toCommentDetailResponse(comment, post, writer);
    }

    public CommentDetailResponse createComment(CommentCreateRequest request) {
        request.normalize();

        Post post = findPostById(request.getPostId());
        Account writer = findAccountById(request.getWriterId());
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

    public void deleteComment(Long commentId) {
        Comment comment = findCommentById(commentId);
        commentRepository.delete(comment);
    }

    public Page<CommentDetailResponse> getCommentsByPost(Long postId, Pageable pageable) {
        Post post = findPostById(postId);
        PostSummary postSummary = PostMapper.toPostSummary(post);

        Page<Comment> comments = commentRepository.findAllByPostId(postId, pageable);
        return comments.map(comment -> CommentMapper.toCommentDetailResponse(
                comment,
                postSummary,
                AccountMapper.toAccountSummary(comment.getWriter())
        ));
    }

    public Page<CommentDetailResponse> getCommentsByAccount(Long accountId, Pageable pageable) {
        Account account = findAccountById(accountId);
        AccountSummary accountSummary = AccountMapper.toAccountSummary(account);

        Page<Comment> comments = commentRepository.findAllByWriterId(accountId, pageable);
        return comments.map(comment -> CommentMapper.toCommentDetailResponse(
                comment,
                PostMapper.toPostSummary(comment.getPost()),
                accountSummary
        ));
    }}

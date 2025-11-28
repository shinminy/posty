package com.posty.postingapi.service.application;

import com.posty.postingapi.domain.comment.Comment;
import com.posty.postingapi.domain.comment.CommentRepository;
import com.posty.postingapi.dto.account.AccountSummary;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.error.ResourceNotFoundException;
import com.posty.postingapi.mapper.AccountMapper;
import com.posty.postingapi.mapper.CommentMapper;
import com.posty.postingapi.mapper.PostMapper;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(
            CommentRepository commentRepository
    ) {
        this.commentRepository = commentRepository;
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
}

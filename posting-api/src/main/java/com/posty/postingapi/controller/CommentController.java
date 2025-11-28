package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.comment.CommentUpdateRequest;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "댓글 관리 API", description = "댓글 관련 CRUD API")
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "댓글 상세정보 조회", description = "댓글의 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/comment/{commentId}")
    public CommentDetailResponse getComment(@PathVariable Long commentId) {
        return commentService.getCommentDetail(commentId);
    }

    @Operation(summary = "댓글 생성", description = "댓글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created")
    @PostMapping("/comment")
    public ResponseEntity<CommentDetailResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentDetailResponse body = commentService.createComment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/comment/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PutMapping("/comment/{commentId}")
    public ResponseEntity<Void> updateComment(@PathVariable Long commentId, @Valid @RequestBody CommentUpdateRequest request) {
        commentService.updateComment(commentId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 삭제", description = "댓글 삭제를 요청합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @DeleteMapping("/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "포스트의 댓글 목록 조회", description = "해당 포스트의 댓글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/comments/posts/{postId}")
    public Page<CommentDetailResponse> getCommentsByPost(
            @PathVariable Long postId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return commentService.getCommentsByPost(postId, pageable);
    }

    @Operation(summary = "계정의 댓글 목록 조회", description = "해당 계정이 작성한 댓글들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/comments/account/{accountId}")
    public Page<CommentDetailResponse> getCommentsByAccount(
            @PathVariable Long accountId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return commentService.getCommentsByAccount(accountId, pageable);
    }
}

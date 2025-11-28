package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CommentDetailResponse.class)))
    @GetMapping("/{commentId}")
    public CommentDetailResponse getComment(@PathVariable Long commentId) {
        return commentService.getCommentDetail(commentId);
    }

    @Operation(summary = "댓글 생성", description = "댓글을 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = CommentDetailResponse.class)))
    @PostMapping
    public ResponseEntity<CommentDetailResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
        CommentDetailResponse body = commentService.createComment(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}

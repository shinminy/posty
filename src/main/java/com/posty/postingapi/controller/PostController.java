package com.posty.postingapi.controller;

import com.posty.postingapi.dto.PostDetail;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "포스트 관리 API", description = "포스트 관련 CRUD API")
@CommonErrorResponses
@Validated
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "포스트 상세정보 조회", description = "내용 일부를 포함한 포스트 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PostDetail.class)))
    @GetMapping("/{post-id}")
    public PostDetail getPost(
            @PathVariable("post-id") Long postId,
            @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) int size
    ) {
        return postService.getPostDetail(postId, page, size);
    }
}

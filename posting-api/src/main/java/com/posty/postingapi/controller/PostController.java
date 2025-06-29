package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.PostCreateRequest;
import com.posty.postingapi.dto.PostDetailResponse;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "포스트 관리 API", description = "포스트 관련 CRUD API")
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "포스트 상세정보 조회", description = "내용 일부를 포함한 포스트 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = PostDetailResponse.class)))
    @GetMapping("/{post-id}")
    public PostDetailResponse getPost(
            @PathVariable("post-id") Long postId,
            @Parameter(description = "포스트 내 블록 목록의 페이지 번호") @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
            @Parameter(description = "포스트 내 블록 목록의 한 페이지 크기") @RequestParam(required = false, defaultValue = "10") @Min(1) int size
    ) {
        return postService.getPostDetail(postId, page, size);
    }

    @Operation(summary = "포스트 생성", description = "포스트를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = PostDetailResponse.class)))
    @PostMapping
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse body = postService.createPost(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}

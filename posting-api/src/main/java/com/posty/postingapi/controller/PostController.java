package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.post.PostCreateRequest;
import com.posty.postingapi.dto.post.PostDetailResponse;
import com.posty.postingapi.dto.post.PostSummary;
import com.posty.postingapi.dto.post.PostUpdateRequest;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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

@Tag(name = "포스트 관리 API", description = "포스트 관련 CRUD API")
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "포스트 상세정보 조회", description = "내용 일부를 포함한 포스트 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/post/{postId}")
    public PostDetailResponse getPost(
            @PathVariable Long postId,
            @Parameter(description = "포스트 내 블록 목록의 페이지") @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @Parameter(description = "포스트 내 블록 목록의 한 페이지 크기") @RequestParam(required = false, defaultValue = "10") @Min(1) int size
    ) {
        return postService.getPostDetail(postId, page, size);
    }

    @Operation(summary = "포스트 생성", description = "포스트를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created")
    @PostMapping("/post")
    public ResponseEntity<PostDetailResponse> createPost(@Valid @RequestBody PostCreateRequest request) {
        PostDetailResponse body = postService.createPost(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/post/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "포스트 수정", description = "포스트를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PutMapping("/post/{postId}")
    public ResponseEntity<Void> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateRequest request) {
        postService.updatePost(postId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "포스트 삭제", description = "포스트 삭제를 요청합니다. 해당 포스트에 속한 미디어 파일도 전부 삭제됩니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "계정의 포스트 목록 조회", description = "해당 계정이 작가인 포스트들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/posts/account/{accountId}")
    public Page<PostSummary> getPostsByWriter(
            @PathVariable Long accountId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postService.getPostsByWriter(accountId, pageable);
    }
}

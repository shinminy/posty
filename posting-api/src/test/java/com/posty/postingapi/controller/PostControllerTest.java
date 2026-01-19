package com.posty.postingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.dto.post.*;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.PostService;
import com.posty.postingapi.support.TestSecurityConfig;
import com.posty.postingapi.support.TestTimeConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import({TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("포스트 상세 조회 API 성공")
    void getPost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostDetailResponse response = new PostDetailResponse(
                postId, "Title", List.of("Writer"), null, null, new PageImpl<>(Collections.emptyList())
        );
        given(postService.getPostDetail(anyLong(), anyInt(), anyInt())).willReturn(response);

        // when & then
        mockMvc.perform(get("/posts/{postId}", postId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("포스트 생성 API 성공")
    void createPost_Success() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest(
                1L, "New Post", List.of(new PostBlockCreateRequest(1, 1L, new TextContentRequest("Hello")))
        );
        PostDetailResponse response = new PostDetailResponse(
                1L, "New Post", List.of("Writer"), null, null, new PageImpl<>(Collections.emptyList())
        );
        given(postService.createPost(any(PostCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Post"));
    }

    @Test
    @DisplayName("포스트 수정 API 성공")
    void updatePost_Success() throws Exception {
        // given
        Long postId = 1L;
        PostUpdateRequest request = new PostUpdateRequest("Updated Title", null, null, null);

        // when & then
        mockMvc.perform(patch("/posts/{postId}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(postService).updatePost(eq(postId), any(PostUpdateRequest.class));
    }

    @Test
    @DisplayName("포스트 삭제 API 성공")
    void deletePost_Success() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(delete("/posts/{postId}", postId))
                .andExpect(status().isNoContent());

        verify(postService).deletePost(postId);
    }

    @Test
    @DisplayName("작가의 포스트 목록 조회 API 성공")
    void getPostsByWriter_Success() throws Exception {
        // given
        Long accountId = 1L;
        given(postService.getPostsByWriter(eq(accountId), any(org.springframework.data.domain.Pageable.class))).willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/posts/writer/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

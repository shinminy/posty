package com.posty.postingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.dto.comment.CommentCreateRequest;
import com.posty.postingapi.dto.comment.CommentDetailResponse;
import com.posty.postingapi.dto.comment.CommentUpdateRequest;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.CommentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import({TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("댓글 상세 조회 API 성공")
    void getComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        CommentDetailResponse response = new CommentDetailResponse(
                commentId, null, "Comment Content", null, null, null
        );
        given(commentService.getCommentDetail(commentId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId))
                .andExpect(jsonPath("$.content").value("Comment Content"));
    }

    @Test
    @DisplayName("댓글 생성 API 성공")
    void createComment_Success() throws Exception {
        // given
        CommentCreateRequest request = new CommentCreateRequest(1L, "New Comment", 1L);
        CommentDetailResponse response = new CommentDetailResponse(
                1L, null, "New Comment", null, null, null
        );
        given(commentService.createComment(any(CommentCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.content").value("New Comment"));
    }

    @Test
    @DisplayName("댓글 수정 API 성공")
    void updateComment_Success() throws Exception {
        // given
        Long commentId = 1L;
        CommentUpdateRequest request = new CommentUpdateRequest("Updated Comment");

        // when & then
        mockMvc.perform(patch("/comments/{commentId}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(commentService).updateComment(eq(commentId), any(CommentUpdateRequest.class));
    }

    @Test
    @DisplayName("댓글 삭제 API 성공")
    void deleteComment_Success() throws Exception {
        // given
        Long commentId = 1L;

        // when & then
        mockMvc.perform(delete("/comments/{commentId}", commentId))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(commentId);
    }

    @Test
    @DisplayName("포스트의 댓글 목록 조회 API 성공")
    void getCommentsByPost_Success() throws Exception {
        // given
        Long postId = 1L;
        given(commentService.getCommentsByPost(eq(postId), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/comments/post/{postId}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("계정의 댓글 목록 조회 API 성공")
    void getCommentsByAccount_Success() throws Exception {
        // given
        Long accountId = 1L;
        given(commentService.getCommentsByAccount(eq(accountId), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/comments/account/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

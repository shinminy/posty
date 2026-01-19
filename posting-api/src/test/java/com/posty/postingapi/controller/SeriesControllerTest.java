package com.posty.postingapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.posty.postingapi.dto.series.SeriesCreateRequest;
import com.posty.postingapi.dto.series.SeriesDetailResponse;
import com.posty.postingapi.dto.series.SeriesUpdateRequest;
import com.posty.postingapi.properties.ApiProperties;
import com.posty.postingapi.service.application.SeriesService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeriesController.class)
@Import({TestSecurityConfig.class, TestTimeConfig.class, ApiProperties.class})
class SeriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SeriesService seriesService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("시리즈 상세 조회 API 성공")
    void getSeries_Success() throws Exception {
        // given
        Long seriesId = 1L;
        SeriesDetailResponse response = new SeriesDetailResponse(
                seriesId, "Series Title", "Description", Collections.emptyList(), Collections.emptyList(), new PageImpl<>(Collections.emptyList())
        );
        given(seriesService.getSeriesDetail(eq(seriesId), any())).willReturn(response);

        // when & then
        mockMvc.perform(get("/series/{seriesId}", seriesId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(seriesId))
                .andExpect(jsonPath("$.title").value("Series Title"));
    }

    @Test
    @DisplayName("시리즈 생성 API 성공")
    void createSeries_Success() throws Exception {
        // given
        SeriesCreateRequest request = new SeriesCreateRequest("New Series", "Description", List.of(1L));
        SeriesDetailResponse response = new SeriesDetailResponse(
                1L, "New Series", "Description", Collections.emptyList(), Collections.emptyList(), new PageImpl<>(Collections.emptyList())
        );
        given(seriesService.createSeries(any(SeriesCreateRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/series")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Series"));
    }

    @Test
    @DisplayName("시리즈 수정 API 성공")
    void updateSeries_Success() throws Exception {
        // given
        Long seriesId = 1L;
        SeriesUpdateRequest request = new SeriesUpdateRequest("Updated Series", "New Description", List.of(1L));

        // when & then
        mockMvc.perform(patch("/series/{seriesId}", seriesId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(seriesService).updateSeries(eq(seriesId), any(SeriesUpdateRequest.class));
    }

    @Test
    @DisplayName("시리즈 삭제 API 성공")
    void deleteSeries_Success() throws Exception {
        // given
        Long seriesId = 1L;

        // when & then
        mockMvc.perform(delete("/series/{seriesId}", seriesId))
                .andExpect(status().isNoContent());

        verify(seriesService).deleteSeries(seriesId);
    }

    @Test
    @DisplayName("계정이 관리하는 시리즈 목록 조회 API 성공")
    void getSeriesByManager_Success() throws Exception {
        // given
        Long accountId = 1L;
        given(seriesService.getSeriesByManager(eq(accountId), any())).willReturn(new PageImpl<>(Collections.emptyList()));

        // when & then
        mockMvc.perform(get("/series/manager/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}

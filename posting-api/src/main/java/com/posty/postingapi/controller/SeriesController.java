package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.series.SeriesCreateRequest;
import com.posty.postingapi.dto.series.SeriesDetailResponse;
import com.posty.postingapi.dto.series.SeriesSummary;
import com.posty.postingapi.dto.series.SeriesUpdateRequest;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.application.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
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

@Tag(name = "시리즈 관리 API", description = "시리즈 관련 CRUD API")
@CommonErrorResponses
@ResponseLogging
@Validated
@RestController
@RequestMapping("/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @Operation(summary = "시리즈 상세정보 조회", description = "포스트 목록을 포함한 시리즈의 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/{seriesId}")
    public SeriesDetailResponse getSeries(
            @PathVariable Long seriesId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return seriesService.getSeriesDetail(seriesId, pageable);
    }
    
    @Operation(summary = "시리즈 생성", description = "시리즈를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created")
    @PostMapping
    public ResponseEntity<SeriesDetailResponse> createSeries(@Valid @RequestBody SeriesCreateRequest request) {
        SeriesDetailResponse body = seriesService.createSeries(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }

    @Operation(summary = "시리즈 수정", description = "시리즈 정보를 수정합니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @PutMapping("/{seriesId}")
    public ResponseEntity<Void> updateSeries(@PathVariable Long seriesId, @Valid @RequestBody SeriesUpdateRequest request) {
        seriesService.updateSeries(seriesId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "시리즈 삭제", description = "시리즈 삭제를 요청합니다. 해당 시리즈에 속한 포스트도 전부 삭제됩니다.")
    @ApiResponse(responseCode = "204", description = "No Content")
    @DeleteMapping("/{seriesId}")
    public ResponseEntity<Void> deleteSeries(@PathVariable Long seriesId) {
        seriesService.deleteSeries(seriesId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "계정이 관리하는 시리즈 목록 조회", description = "해당 계정이 관리 중인 시리즈들을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping("/series/manager/{accountId}")
    public Page<SeriesSummary> getSeriesByManager(
            @PathVariable Long accountId,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return seriesService.getSeriesByManager(accountId, pageable);
    }
}

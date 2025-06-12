package com.posty.postingapi.controller;

import com.posty.postingapi.aspect.ResponseLogging;
import com.posty.postingapi.dto.SeriesCreateRequest;
import com.posty.postingapi.dto.SeriesDetailResponse;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.SeriesService;
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
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SeriesDetailResponse.class)))
    @GetMapping("/{seriesId}")
    public SeriesDetailResponse getSeries(
            @PathVariable Long seriesId,
            @Parameter(description = "시리즈 내 포스트 목록의 페이지 번호") @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
            @Parameter(description = "시리즈 내 포스트 목록의 한 페이지 크기") @RequestParam(required = false, defaultValue = "10") @Min(1) int size
    ) {
        return seriesService.getSeriesDetail(seriesId, page, size);
    }
    
    @Operation(summary = "시리즈 생성", description = "시리즈를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = SeriesDetailResponse.class)))
    @PostMapping
    public ResponseEntity<SeriesDetailResponse> createSeries(@Valid @RequestBody SeriesCreateRequest request) {
        SeriesDetailResponse body = seriesService.createSeries(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.getId())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}

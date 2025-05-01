package com.posty.postingapi.controller;

import com.posty.postingapi.dto.SeriesDetail;
import com.posty.postingapi.error.CommonErrorResponses;
import com.posty.postingapi.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "시리즈 관리 API", description = "시리즈 관련 CRUD API")
@CommonErrorResponses
@Validated
@RestController
@RequestMapping("/series")
public class SeriesController {

    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    @Operation(summary = "시리즈 상세정보 조회", description = "포스트 목록을 포함한 시리즈의 상세정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = SeriesDetail.class)))
    @GetMapping("/{series-id}")
    public SeriesDetail getSeries(
            @PathVariable("series-id") Long seriesId,
            @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) int size
    ) {
        return seriesService.getSeriesDetail(seriesId, page, size);
    }
}

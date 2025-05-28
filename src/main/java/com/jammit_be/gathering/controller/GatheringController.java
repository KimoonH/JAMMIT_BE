package com.jammit_be.gathering.controller;

import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.service.GatheringService;
import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "모임", description = "모임 관련 API")
@RestController
@RequestMapping("/jammit/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;

    @Operation(
            summary = "모임 등록 API", description = "새로운 모임을 생성한다."
            ,responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "모임 등록 성공"
                    )
                    ,@ApiResponse(
                    responseCode = "400",
                    description = "입력값 오류, 등록 실패")
                    ,@ApiResponse(
                    responseCode = "403",
                    description = "권한 없음(로그인 필요)")
            }
    )
    @PostMapping
    public CommonResponse<GatheringCreateResponse> createGathering(@RequestBody GatheringCreateRequest request, @AuthenticationPrincipal User user) {
        GatheringCreateResponse response = gatheringService.createGathering(request, user);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "모임 전체 목록 조회",
            description = "음악 장르/세션별 필터, 페이징, 정렬로 모임 리스트를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping
    public GatheringListResponse getGatherings(
            @Parameter(description = "음악 장르 (예: ROCK, JAZZ 등). 복수 선택 가능", example = "ROCK")
            @RequestParam(required = false) List<Genre> genres,
            @Parameter(description = "모집 세션(예: VOCAL, DRUM 등). 복수 선택 가능", example = "VOCAL")
            @RequestParam(required = false) List<BandSession> sessions,
            @Parameter(hidden = true) Pageable pageable
            ){

        return gatheringService.findGatherings(genres, sessions, pageable);
    }
}

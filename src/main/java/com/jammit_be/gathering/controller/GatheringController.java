package com.jammit_be.gathering.controller;

import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.Genre;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.request.GatheringUpdateRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.dto.response.GatheringDetailResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.service.GatheringService;
import com.jammit_be.gathering.service.GatheringParticipationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "모임", description = "모임 관련 API")
@RestController
@RequestMapping("/jammit/gatherings")
@RequiredArgsConstructor
public class GatheringController {

    private final GatheringService gatheringService;
    private final GatheringParticipationService gatheringParticipationService;


    @Operation(
            summary = "모임 전체 목록 조회 API",
            description = "음악 장르/세션별 필터, 페이징, 정렬로 모임 리스트를 조회합니다. (로그인 없이 사용 가능)<br>" +
                    "        <b>정렬 파라미터 사용 예시</b>:<br>\n" +
                    "        • <code>?sort=viewCount,desc</code><br>\n" +
                    "        • <code>?sort=recruitDeadline,asc</code><br>\n" +
                    "        • 여러 정렬 조건: <code>?sort=viewCount,desc&sort=recruitDeadline,asc</code>",
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
            @Parameter(description = "모집 세션(예: VOCAL, DRUM, KEYBOARD 등). 복수 선택 가능", example = "VOCAL")
            @RequestParam(required = false) List<BandSession> sessions,
            @ParameterObject Pageable pageable
    ){

        return gatheringService.findGatherings(genres, sessions, pageable);
    }

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
    public CommonResponse<GatheringCreateResponse> createGathering(@RequestBody GatheringCreateRequest request) {
        GatheringCreateResponse response = gatheringService.createGathering(request);
        return CommonResponse.ok(response);
    }


    @Operation(
            summary = "모임 상세 조회 API",
            description = "모임의 상세 정보를 조회합니다. (로그인 없이 사용 가능)",
            parameters = {
                    @Parameter(name = "id", description = "Gathering PK", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GatheringDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 모임")
            }
    )
    @GetMapping("/{id}")
    public GatheringDetailResponse getGatheringDetail(@PathVariable Long id) {
        return gatheringService.getGatheringDetail(id);
    }

    @Operation(
            summary = "모임 수정 API",
            description = "모임 정보(이름, 장소, 날짜 등)를 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력값 오류"),
                    @ApiResponse(responseCode = "403", description = "권한 없음/로그인 필요"),
                    @ApiResponse(responseCode = "404", description = "모임이 존재하지 않음")
            }
    )
    @PutMapping("/{id}")
    public CommonResponse<GatheringDetailResponse> updateGathering(
            @PathVariable Long id,
            @RequestBody GatheringUpdateRequest request
    ) {
        GatheringDetailResponse response = gatheringService.updateGathering(id, request);
        return CommonResponse.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "모임 개설 취소 API",
            description = "모임을 개설 취소 상태로 변경합니다. 취소된 모임은 조회는 가능하지만 새로운 참가자를 받을 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모임 취소 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음/로그인 필요"),
                    @ApiResponse(responseCode = "404", description = "모임이 존재하지 않음")
            }
    )
    public CommonResponse<Void> cancelGathering(
            @Parameter(description = "취소할 모임 ID", example = "1")
            @PathVariable Long id
    ) {
        gatheringService.cancelGathering(id);
        return CommonResponse.ok();
    }

    @Operation(
            summary = "내가 생성한 모임 목록 조회 API",
            description = "로그인한 사용자가 생성한 모임 목록을 조회합니다. 취소된 모임도 조회할 수 있습니다.",
            parameters = {
                    @Parameter(name = "includeCanceled", description = "취소된 모임 포함 여부", example = "false"),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지 크기", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요")
            }
    )
    @GetMapping("/my/created")
    public CommonResponse<GatheringListResponse> getMyCreatedGatherings(
            @RequestParam(required = false, defaultValue = "false") boolean includeCanceled,
            @Parameter(hidden = true) Pageable pageable
    ) {
        GatheringListResponse myGatherings = gatheringService.getMyCreatedGatherings(includeCanceled, pageable);
        return CommonResponse.ok(myGatherings);
    }

    @Operation(
            summary = "모임 완료 처리 API",
            description = "실제 합주가 완료되었음을 표시하고, 모임을 완료 상태로 변경합니다. 완료된 모임의 참가자들은 서로를 리뷰할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모임 완료 처리 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음/로그인 필요"),
                    @ApiResponse(responseCode = "404", description = "모임이 존재하지 않음")
            }
    )
    @PutMapping("/{id}/complete")
    public CommonResponse<Void> completeGathering(
            @Parameter(description = "완료 처리할 모임 ID", example = "1")
            @PathVariable Long id
    ) {
        gatheringParticipationService.completeGathering(id);
        return CommonResponse.ok();
    }

}

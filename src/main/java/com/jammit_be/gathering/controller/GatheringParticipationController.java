package com.jammit_be.gathering.controller;

import com.jammit_be.auth.util.AuthUtil;
import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.gathering.dto.GatheringSummary;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
import com.jammit_be.gathering.dto.response.CompletedGatheringResponse;
import com.jammit_be.gathering.dto.response.GatheringListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipantListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.service.GatheringParticipationService;
import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "모임참가", description = "모임 참여 관련 API")
@RestController
@RequestMapping("/jammit/gatherings/{gatheringId}/participants")
@RequiredArgsConstructor
public class GatheringParticipationController {

    private final GatheringParticipationService gatheringParticipationService;


    @Operation(
            summary = "모임 참여 신청",
            description = "지정한 모임(gatheringId)에 로그인 유저가 원하는 파트로 참여를 신청합니다. 중복 신청, 정원 초과 등 예외 발생 가능.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "참여할 모임 PK", example = "1")
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GatheringParticipationRequest.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "참여 신청 성공",
                            content = @Content(schema = @Schema(implementation = GatheringParticipationResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청, 중복 신청, 정원 마감 등 실패"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @PostMapping
    public CommonResponse<GatheringParticipationResponse> participate(
            @PathVariable Long gatheringId,
            @RequestBody GatheringParticipationRequest request
    ) {
        GatheringParticipationResponse response = gatheringParticipationService.participate(gatheringId, request);
        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "모임 참가 신청 취소 API",
            description = "로그인한 사용자가 본인의 참가 신청을 취소합니다. (논리적 삭제, 실제 DB 삭제 아님)",
            parameters = {
                    @Parameter(name = "gatheringId", description = "모임 PK", example = "1"),
                    @Parameter(name = "participantId", description = "참가 신청 PK", example = "100")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "참가 취소 성공",
                            content = @Content(schema = @Schema(implementation = GatheringParticipationResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "이미 취소된 참가 신청"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 필요"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음(본인만 가능)"
                    )
            }
    )
    @PutMapping("/{participantId}/cancel")
    public CommonResponse<GatheringParticipationResponse> cancelParticipation(
            @PathVariable("gatheringId") Long gatheringId,
            @PathVariable("participantId") Long participantId
    ) {
        GatheringParticipationResponse response =
                gatheringParticipationService.cancelParticipation(gatheringId, participantId);
        return CommonResponse.ok(response);
    }


    @Operation(
            summary = "모임 참가자 승인 API",
            description = "밴드 모임 주최자가 참가자를 승인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "참가자 승인 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 승인된 참가자 또는 정원 초과 등"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (주최자만 가능)")
            }
    )
    @PostMapping("/{participantId}/approve")
    public CommonResponse<GatheringParticipationResponse> approveParticipant(
            @PathVariable("gatheringId") Long gatheringId,
            @PathVariable("participantId") Long participantId
    ) {
        GatheringParticipationResponse response = gatheringParticipationService
                .approveParticipation(gatheringId, participantId);


        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "참가자 거절 API",
            description = "주최자가 해당 모임 참가 신청을 거절합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "거절 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 승인/거절/취소됨 또는 권한 없음"),
                    @ApiResponse(responseCode = "404", description = "참가 신청 없음"),
            }
    )
    @PutMapping("/{participantId}/reject")
    public CommonResponse<GatheringParticipationResponse> rejectParticipant(
            @PathVariable("gatheringId") Long gatheringId,
            @PathVariable("participantId") Long participantId
    ) {
        GatheringParticipationResponse response = gatheringParticipationService
                .rejectParticipation(gatheringId, participantId);

        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "모임 참가자 목록 조회 API",
            description = "지정한 모임(gatheringId)에 참가한 전체 참가자(신청자/승인자/취소/거절 포함) 목록을 반환합니다.",
            parameters = {
                    @Parameter(name = "gatheringId", description = "조회할 모임 PK", example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "정상적으로 참가자 리스트 반환",
                            content = @Content(schema = @Schema(implementation = GatheringParticipantListResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "모임이 없거나, 참가자가 없는 경우(빈 배열, total=0 반환)"
                    )
            }
    )
    @GetMapping
    public CommonResponse<GatheringParticipantListResponse> getParticipants(@PathVariable Long gatheringId) {
        GatheringParticipantListResponse response = gatheringParticipationService.findParticipants(gatheringId);

        return CommonResponse.ok(response);
    }

    @Operation(
            summary = "내가 신청한 모임 목록 조회 API",
            description = "로그인한 사용자가 신청한 모임 목록을 조회합니다. 취소된 모임도 조회할 수 있습니다.",
            parameters = {
                    @Parameter(name = "includeCanceled", description = "취소된 모임 포함 여부", example = "true"),
                    @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
                    @Parameter(name = "size", description = "페이지 크기", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요")
            }
    )
    @GetMapping("/my")
    public CommonResponse<GatheringListResponse> getMyParticipations(
            @Parameter(hidden = true) Pageable pageable
    ) {
        GatheringListResponse myParticipations = gatheringParticipationService.getMyParticipations(pageable);
        return CommonResponse.ok(myParticipations);
    }

    @Operation(
            summary = "참여 완료 + 모임 완료 상태인 모임 목록 조회",
            description = "로그인한 사용자가 참여 완료한 모임 중, 모임 상태가 COMPLETED인 모임 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CompletedGatheringResponse.class)))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증 필요 (토큰 없음 또는 만료됨)"),
                    @ApiResponse(responseCode = "403", description = "접근 권한 없음")
            }
    )
    @GetMapping("/my/completed")
    public CommonResponse<List<CompletedGatheringResponse>> getMyCompletedGatherings(
            @AuthenticationPrincipal User currentUser
    ) {
        List<CompletedGatheringResponse> response =
                gatheringParticipationService.getMyCompletedGatherings(currentUser);

        return CommonResponse.ok(response);
    }

}

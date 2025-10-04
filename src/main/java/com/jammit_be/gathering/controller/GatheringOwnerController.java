package com.jammit_be.gathering.controller;

import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipantListResponse;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.service.GatheringOwnerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "모임관리", description = "모임 주최자 관리 API")
@RestController
@RequestMapping("/jammit/gatherings/{gatheringId}")
@RequiredArgsConstructor
public class GatheringOwnerController {

    private final GatheringOwnerService gatheringOwnerService;

    @Operation(
            summary = "모임 참가자 승인 API",
            description = "밴드 모임 주최자가 참가자를 승인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "참가자 승인 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 승인된 참가자 또는 정원 초과 등"),
                    @ApiResponse(responseCode = "403", description = "권한 없음 (주최자만 가능)")
            }
    )
    @PostMapping("/participants/{participantId}/approve")
    public CommonResponse<GatheringParticipationResponse> approveParticipant(
            @PathVariable("gatheringId") Long gatheringId,
            @PathVariable("participantId") Long participantId
    ) {
        GatheringParticipationResponse response = gatheringOwnerService
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
    @PutMapping("/participants/{participantId}/reject")
    public CommonResponse<GatheringParticipationResponse> rejectParticipant(
            @PathVariable("gatheringId") Long gatheringId,
            @PathVariable("participantId") Long participantId
    ) {
        GatheringParticipationResponse response = gatheringOwnerService
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
    @GetMapping("/participants")
    public CommonResponse<GatheringParticipantListResponse> getParticipants(
            @PathVariable Long gatheringId
    ) {
        GatheringParticipantListResponse response = gatheringOwnerService.findParticipants(gatheringId);

        return CommonResponse.ok(response);
    }


}

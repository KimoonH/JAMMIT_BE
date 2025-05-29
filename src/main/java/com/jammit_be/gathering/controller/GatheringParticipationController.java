package com.jammit_be.gathering.controller;

import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.common.enums.BandSession;
import com.jammit_be.gathering.dto.request.GatheringParticipationRequest;
import com.jammit_be.gathering.dto.response.GatheringParticipationResponse;
import com.jammit_be.gathering.service.GatheringParticipationService;
import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
            @RequestBody GatheringParticipationRequest request,
            @AuthenticationPrincipal User user
    ) {
        GatheringParticipationResponse response = gatheringParticipationService.participate(gatheringId, request, user);
        return CommonResponse.ok(response);
    }

}

package com.jammit_be.gathering.controller;

import com.jammit_be.common.dto.CommonResponse;
import com.jammit_be.gathering.dto.request.GatheringCreateRequest;
import com.jammit_be.gathering.dto.response.GatheringCreateResponse;
import com.jammit_be.gathering.service.GatheringService;
import com.jammit_be.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "모임", description = "모임 관련 API")
@RestController
@RequestMapping("/jammit/gathering")
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
}

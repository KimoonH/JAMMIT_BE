package com.jammit_be.gathering.dto.response;

import com.jammit_be.common.enums.BandSession;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GatheringParticipationResponse {
    @Schema(description = "모임 아이디 PK", example = "1")
    private final Long gatheringId; // 참여 신청한 모임의 식별자 PK
    @Schema(description = "유저 아이디 PK", example = "10")
    private final Long userId; // 참여 신청자(유저)의 식별자 PK
    @Schema(description = "신청 밴드 세션", example = "VOCAL")
    private final BandSession bandSession; //신청한 밴드 세션(파트) 종류
    @Schema(description = "승인 여부", example = "false")
    private final boolean approved; // 승인 여부 (주최자 승인 전이면 false)
    @Schema(description = "결과 메시지", example = "참여 신청이 완료되었습니다. 승인 대기 중입니다.")
    private final String message; // 응답 메시지

    public static GatheringParticipationResponse waiting(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .approved(false)
                .message("참여 신청이 완료되었습니다. 승인 대기 중입니다.")
                .build();
    }

    public static GatheringParticipationResponse approved(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .approved(true)
                .message("참여가 승인되었습니다.")
                .build();
    }

    public static GatheringParticipationResponse fail(String message) {
        return GatheringParticipationResponse.builder()
                .approved(false)
                .message(message)
                .build();
    }
}

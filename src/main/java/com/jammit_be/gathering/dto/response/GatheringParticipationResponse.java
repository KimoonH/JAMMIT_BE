package com.jammit_be.gathering.dto.response;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "모임 참가 신청 응답")
public class GatheringParticipationResponse {
    @Schema(description = "모임 아이디 PK", example = "1")
    private final Long gatheringId; // 참여 신청한 모임의 식별자 PK
    @Schema(description = "유저 아이디 PK", example = "10")
    private final Long userId; // 참여 신청자(유저)의 식별자 PK
    @Schema(description = "신청 밴드 세션", example = "KEYBOARD")
    private final BandSession bandSession; //신청한 밴드 세션(파트) 종류
    @Schema(description = "참가자 상태", example = "PENDING", 
            allowableValues = {"PENDING", "APPROVED", "REJECTED", "COMPLETED", "CANCELED"},
            implementation = ParticipantStatus.class)
    private final ParticipantStatus status; // 참가자 상태
    @Schema(description = "결과 메시지", example = "참여 신청이 완료되었습니다. 승인 대기 중입니다.")
    private final String message; // 응답 메시지

    public static GatheringParticipationResponse waiting(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .status(ParticipantStatus.PENDING)
                .message("참여 신청이 완료되었습니다. 승인 대기 중입니다.")
                .build();
    }

    public static GatheringParticipationResponse approved(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .status(ParticipantStatus.APPROVED)
                .message("참여가 승인되었습니다.")
                .build();
    }

    public static GatheringParticipationResponse fail(String message) {
        return GatheringParticipationResponse.builder()
                .status(null)
                .message(message)
                .build();
    }

    public static GatheringParticipationResponse canceled(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .status(ParticipantStatus.CANCELED)
                .message("참여가 취소되었습니다.")
                .build();
    }

    public static GatheringParticipationResponse rejected(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .status(ParticipantStatus.REJECTED)
                .message("참여 요청이 거절되었습니다.")
                .build();
    }
    
    public static GatheringParticipationResponse completed(Long gatheringId, Long userId, BandSession bandSession) {
        return GatheringParticipationResponse.builder()
                .gatheringId(gatheringId)
                .userId(userId)
                .bandSession(bandSession)
                .status(ParticipantStatus.COMPLETED)
                .message("모임 참여가 완료되었습니다.")
                .build();
    }
    
    /**
     * 승인 상태인지 확인합니다.
     * @return 승인 여부
     */
    public boolean isApproved() {
        return status != null && status.isApproved();
    }
    
    /**
     * 취소 상태인지 확인합니다.
     * @return 취소 여부
     */
    public boolean isCanceled() {
        return status == ParticipantStatus.CANCELED;
    }
    
    /**
     * 거절 상태인지 확인합니다.
     * @return 거절 여부
     */
    public boolean isRejected() {
        return status == ParticipantStatus.REJECTED;
    }
    
    /**
     * 대기 상태인지 확인합니다.
     * @return 대기 여부
     */
    public boolean isPending() {
        return status == ParticipantStatus.PENDING;
    }
    
    /**
     * 참여 완료 상태인지 확인합니다.
     * @return 참여 완료 여부
     */
    public boolean isCompleted() {
        return status == ParticipantStatus.COMPLETED;
    }
}

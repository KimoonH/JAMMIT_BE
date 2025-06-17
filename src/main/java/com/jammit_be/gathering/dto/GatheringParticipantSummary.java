package com.jammit_be.gathering.dto;

import com.jammit_be.common.enums.BandSession;
import com.jammit_be.common.enums.ParticipantStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "모임 참가자 요약 정보")
public class GatheringParticipantSummary {
    @Schema(description = "참가자 신청 PK", example = "1")
    private final Long participantId;           // 참가자 신청 PK
    @Schema(description = "유저 PK", example = "10")
    private final Long userId;                  // 유저 PK
    @Schema(description = "유저 닉네임", example = "기타왕")
    private final String userNickname;          // 유저 닉네임
    @Schema(description = "유저 이메일", example = "user@example.com")
    private final String userEmail;             // 유저 이메일
    @Schema(description = "신청 파트", example = "GUITAR")
    private final BandSession bandSession;      // 신청 파트
    @Schema(description = "참가자 상태", example = "PENDING", 
            allowableValues = {"PENDING", "APPROVED", "REJECTED", "COMPLETED", "CANCELED"},
            implementation = ParticipantStatus.class)
    private final ParticipantStatus status;
    @Schema(description = "신청 일시", example = "2023-01-01T12:00:00")
    private final LocalDateTime createdAt;      // 신청일시
    @Schema(description = "참여자 소개 문구", example = "안녕하세요! 기타 연주자입니다.")
    private final String introduction;          // 참여자 소개 문구
    @Schema(description = "유저 프로필 이미지", example = "2024/01/11/uuid-profile.jpg")
    private final String userProfileImagePath;
}

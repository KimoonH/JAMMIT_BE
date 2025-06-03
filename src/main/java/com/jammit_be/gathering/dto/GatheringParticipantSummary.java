package com.jammit_be.gathering.dto;

import com.jammit_be.common.enums.BandSession;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GatheringParticipantSummary {
    private final Long participantId;           // 참가자 신청 PK
    private final Long userId;                  // 유저 PK
    private final String userNickname;          // 유저 닉네임
    private final String userEmail;             // 유저 이메일
    private final BandSession bandSession;      // 신청 파트
    private final boolean approved;             // 승인 여부
    private final boolean canceled;             // 취소 여부
    private final boolean rejected;             // 거절 여부
    private final LocalDateTime createdAt;      // 신청일시
    private final String introduction;          // 참여자 소개 문구
}
